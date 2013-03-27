/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.commons.CSVLogger;
import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.core.EvolutionMethod;
import evosimbad.mo.MultiObjectiveFitnessFunction;
import evosimbad.mo.MultiObjectiveNEAT;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.neat4j.core.AIConfig;
import org.neat4j.neat.applications.train.NEATGATrainingManager;
import org.neat4j.neat.core.*;
import org.neat4j.neat.core.mutators.NEATMutator;
import org.neat4j.neat.core.pselectors.TournamentSelector;
import org.neat4j.neat.core.xover.NEATCrossover;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 *
 * @author Jorge
 */
public abstract class NEATEvolution extends EvolutionMethod {

    protected NEATGeneticAlgorithm train;
    protected String configurationPath = "src/evosimbad/evolution/baseNEATConfig.ga";
    protected Map<Chromosome, List<EvaluationFunction>> experimentResults = null;
    protected Map<Chromosome, double[]> scores = null;
    protected Map<Chromosome, Double> fitnessScores = null;
    protected Stats scoreStats, fitnessStats, neatNeuronStats, neatLinkStats;
    private boolean harmonicMean = false;

    public NEATEvolution(int numberGenerations) {
        super(numberGenerations);
    }

    public void setConfigurationPath(String path) {
        this.configurationPath = path;
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        if (main.getExtraOptions().containsKey("harmonicMean")) {
            this.harmonicMean = main.getExtraOptions().getBoolean("harmonicMean");
        }

        // reset innovation db
        InnovationDatabase.resetInnovationDatabase();

        // base config
        AIConfig config = new NEATLoader().loadConfig(configurationPath);
        try {
            FileUtils.copyFileToDirectory(new File(configurationPath), ((CSVLogger) main.getLogger()).getTempFolder());
        } catch (IOException ex) {
            Logger.getLogger(NEATEvolution.class.getName()).log(Level.SEVERE, null, ex);
        }

        // overrided params in exp configuration
        for (String key : main.getExtraOptions().keySet()) {
            if (config.configElement(key) != null) {
                config.updateConfig(key, main.getExtraOptions().get(key));
                System.out.println("Override NEAT param " + key + ": " + config.configElement(key));
            }
        }

        // create GA
        NEATGATrainingManager gam = new NEATGATrainingManager();
        NEATGADescriptor gaDescriptor = (NEATGADescriptor) gam.createDescriptor(config);
        gaDescriptor.setInputNodes(main.getAgentGen().getInputs());
        gaDescriptor.setOutputNodes(main.getAgentGen().getOutputs());

        boolean mo = main.getExtraOptions().containsKey("multiobjective") && main.getExtraOptions().getBoolean("multiobjective");
        if (mo) {
            boolean diversity = main.getExtraOptions().containsKey("diversityobjective") && main.getExtraOptions().getBoolean("diversityobjective");
            train = new PreEvaluatedMultiObjective((NEATGADescriptor) gaDescriptor, diversity);
            train.pluginFitnessFunction(new MOFitnessFunction());
            main.getLogger().appendLine("moLog", "Generation","Rank","Crowding Distance", "Score", "Dominated by", "Dominates", "n");
        } else {
            train = new PreEvaluatedSingleObjective((NEATGADescriptor) gaDescriptor);
            train.pluginFitnessFunction(new SingleObjectiveFitnessFunction());
        }

        train.pluginCrossOver(new NEATCrossover());
        train.pluginMutator(new NEATMutator());
        train.pluginParentSelector(new TournamentSelector());
        train.createPopulation();

        // init log
        main.getLogger().appendLine("fitLog",
                "Generation", "Best so far", "Average fit", "Best neurons", "Best links", "Max neurons",
                "Avg neurons", "Min neurons", "Max links", "Avg links", "Min links", "Number species",
                "Lowest fit", "First decile", "Lower quartile", "Median", "Upper quartile", "Last decile", "Best fit",
                "Best score", "Avg score", "Lowest score");

        // continue from previous evolution
        if (main.getExtraOptions().containsKey("continue")) {
            File folder = new File(main.getExtraOptions().get("continue"));
            try {
                ObjectInputStream in = new ObjectInputStream(
                        new BufferedInputStream(
                        new FileInputStream(
                        new File(folder, "lastPopulation.ser"))));
                Chromosome[] genes = (Chromosome[]) in.readObject();
                train.population().updatePopulation(genes);
                in = new ObjectInputStream(
                        new BufferedInputStream(
                        new FileInputStream(
                        new File(folder, "innovationDB.ser"))));
                Object db = in.readObject();
                InnovationDatabase.restoreInnovationDatabase(db);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(NEATEvolution.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void evolutionIteration() {
        super.evolutionIteration();

        /*
         * Save current population
         */
        main.getLogger().saveObject(train.population().genoTypes(), "lastPopulation.ser");
        main.getLogger().saveObject(InnovationDatabase.database(), "innovationDB.ser");

        train.runEpoch();

        // analyse fitness profile of the population - Bowley's seven-figure summary
        ArrayList<Double> sortedScores = new ArrayList<>(fitnessScores.values());
        Collections.sort(sortedScores);
        double firstDecile = sortedScores.get((int) Math.round(sortedScores.size() * 0.10));
        double lowerQuartile = sortedScores.get((int) Math.round(sortedScores.size() * 0.25));
        double median = sortedScores.get((int) Math.round(sortedScores.size() * 0.50));
        double upperQuartile = sortedScores.get((int) Math.round(sortedScores.size() * 0.75));
        double lastDecile = sortedScores.get((int) Math.round(sortedScores.size() * 0.90));

        NEATNeuralNet net = decodeChromosome(fitnessStats.bestChromosome);
        int bestLinks = 0;
        for (NEATNeuron neuron : net.neurons()) {
            bestLinks += neuron.incomingSynapses().size();
        }
        main.getLogger().appendLine("fitLog",
                trainIteration,
                bestFitness,
                fitnessStats.getAverageScore(),
                net.neurons().length,
                bestLinks,
                neatNeuronStats.bestScore,
                neatNeuronStats.getAverageScore(),
                neatNeuronStats.worstScore,
                neatLinkStats.bestScore,
                neatLinkStats.getAverageScore(),
                neatLinkStats.worstScore,
                train.getSpecies().specieList().size(),
                fitnessStats.worstScore,
                firstDecile,
                lowerQuartile,
                median,
                upperQuartile,
                lastDecile,
                fitnessStats.bestScore,
                scoreStats.bestScore,
                scoreStats.getAverageScore(),
                scoreStats.worstScore);
    }

    private class PreEvaluatedSingleObjective extends NEATGeneticAlgorithm {

        public PreEvaluatedSingleObjective(NEATGADescriptor descriptor) {
            super(descriptor);
        }

        @Override
        protected void evaluatePopulation(Chromosome[] genoTypes) {
            experimentResults = experimentPopulation(genoTypes);
            fitnessScores = calculateFitnessScores(experimentResults);
            scores = calculateScores(experimentResults);
            super.evaluatePopulation(genoTypes);
            calculateScoreStats();
            calculateFitnessStats();
            calculateNEATStats();
        }
    }

    private class SingleObjectiveFitnessFunction extends NEATFitnessFunction {

        public SingleObjectiveFitnessFunction() {
            super(null, null);
        }

        @Override
        public double evaluate(Chromosome genoType) {
            return scores.get(genoType)[0];
        }
    }

    private class PreEvaluatedMultiObjective extends MultiObjectiveNEAT {

        public PreEvaluatedMultiObjective(NEATGADescriptor descriptor, boolean diversityObjective) {
            super(descriptor, diversityObjective);
        }

        @Override
        protected void evaluatePopulation(Chromosome[] genoTypes) {
            experimentResults = experimentPopulation(genoTypes);
            fitnessScores = calculateFitnessScores(experimentResults);
            scores = calculateScores(experimentResults);
            super.evaluatePopulation(genoTypes);
            calculateScoreStats();
            calculateFitnessStats();
            calculateNEATStats();
        }

        @Override
        public void runEvolutionCycle(Chromosome[] currentGen) {
            super.runEvolutionCycle(currentGen);
            // Logs
            for(Individual i : super.parentPopulationP) {
                // "Generation","Rank","Crowding Distance", "Score", "Dominated by", "Dominates", n, objectives
                Object[] l = new Object[]{trainIteration, i.getRank(), i.getCrowdingDistance(), i.getChromosome().fitness(), i.getDominatedBy().size() ,i.getS().size(), i.getN()};
                l = ArrayUtils.addAll(l, (Object[]) ArrayUtils.toObject(i.getEvaluation()));
                main.getLogger().appendLine("moLog", l);
            }
        }
    }

    private class MOFitnessFunction extends MultiObjectiveFitnessFunction {

        public MOFitnessFunction() {
            super(null, null);
        }

        @Override
        public double[] evaluateObjectives(Chromosome genoType) {
            return scores.get(genoType);
        }
    }

    protected Map<Chromosome, List<EvaluationFunction>> experimentPopulation(Chromosome[] genoTypes) {
        HashMap<NeuralNet, Chromosome> netMap = new HashMap<>(genoTypes.length * 2);
        for (Chromosome g : genoTypes) {
            NeuralNet n = decodeChromosome(g);
            netMap.put(n, g);
        }
        Map<NeuralNet, List<EvaluationFunction>> results =
                main.getSimulator().runExperiments(netMap.keySet());
        Map<Chromosome, List<EvaluationFunction>> res = new HashMap<>(genoTypes.length * 2);
        for (Entry<NeuralNet, List<EvaluationFunction>> entry : results.entrySet()) {
            res.put(netMap.get(entry.getKey()), entry.getValue());
        }
        return res;
    }

    protected abstract Map<Chromosome, double[]> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations);

    protected Map<Chromosome, Double> calculateFitnessScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        Map<Chromosome, Double> fitScores = new HashMap<>(evaluations.size() * 2);
        for (Entry<Chromosome, List<EvaluationFunction>> e : evaluations.entrySet()) {
            List<EvaluationFunction> samples = e.getValue();
            double fit = 0;
            if (harmonicMean) { // harmonic mean
                for (EvaluationFunction ef : samples) {
                    double f = Math.max(ef.getFitness(), 0.0001);
                    fit += 1 / f;
                }
                fit = samples.size() / fit;
            } else {
                for (EvaluationFunction ef : samples) {
                    double f = ef.getFitness();
                    fit += f;
                }
                fit /= samples.size();
            }
            fitScores.put(e.getKey(), fit);
        }
        return fitScores;
    }

    protected void calculateScoreStats() {
        scoreStats = new Stats();
        for (Chromosome g : train.population().genoTypes()) {
            double score = g.fitness();
            scoreStats.process(g, score);
        }
    }

    protected void calculateFitnessStats() {
        fitnessStats = new Stats();
        for (Chromosome g : train.population().genoTypes()) {
            double fit = fitnessScores.get(g);
            fitnessStats.process(g, fit);
        }
        NeuralNet bestNet = decodeChromosome(fitnessStats.bestChromosome);
        if (fitnessStats.bestScore >= super.bestFitness) {
            super.bestFitness = fitnessStats.bestScore;
            super.bestIndividual = bestNet;
            main.getLogger().saveIndividual(bestIndividual, "everBests/eb" + trainIteration);
            main.getLogger().traceBehaviour(bestIndividual, "everBest/eb" + trainIteration);
        }
        main.getLogger().saveIndividual(bestNet, "bests/best" + trainIteration);
    }

    protected void calculateNEATStats() {
        neatNeuronStats = new Stats();
        neatLinkStats = new Stats();
        for (Chromosome g : train.population().genoTypes()) {
            NEATNeuralNet net = decodeChromosome(g);
            double neurons = net.neurons().length;
            neatNeuronStats.process(g, neurons);
            double links = 0;
            for (NEATNeuron neuron : net.neurons()) {
                links += neuron.incomingSynapses().size();
            }
            neatLinkStats.process(g, links);
        }
    }
}