/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import au.com.bytecode.opencsv.CSVReader;
import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.core.EvolutionMethod;
import evosimbad.core.ExtraOptions;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.Pair;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public abstract class BaseNoveltyImplementation<C extends Serializable> extends NEATEvolution {

    protected int repoSizeLimit = Integer.MAX_VALUE;
    protected double thresholdAdjust = 0.1;
    protected int maximumAdded = 3;
    protected int minimumAdded = 1;
    protected int k;
    protected double threshold;
    protected double tInitial;
    protected boolean usePopulation = true, useRepository = true;
    protected boolean randomCriteria = false;
    protected HashMap<Chromosome, C> populationBehaviours;
    protected HashMap<Chromosome, Pair<Double, C>> noveltyRepository = new LinkedHashMap<>();
    protected EvolutionMethod.Stats repoComparisionStats, repositoryFitnessStats, noveltyStats, distanceStats;
    protected NoveltyFitnessMixer mixer = null;
    protected int repoIndividualId = 0;

    public BaseNoveltyImplementation(int numberGenerations, int k, double tInitial) {
        super(numberGenerations);
        this.tInitial = tInitial;
        this.threshold = tInitial;
        this.k = k;
        this.repositoryFitnessStats = new EvolutionMethod.Stats();
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);

        /*
         * Load extra options
         */
        ExtraOptions options = main.getExtraOptions();
        if (options.containsKey("repositoryMaxAdded")) {
            maximumAdded = options.getInteger("repositoryMaxAdded");
        }
        if (options.containsKey("repositoryMinAdded")) {
            minimumAdded = options.getInteger("repositoryMinAdded");
        }
        if (options.containsKey("repositoryThresholdAdjust")) {
            thresholdAdjust = options.getDouble("repositoryThresholdAdjust");
        }
        if (options.containsKey("noveltyUsePopulation")) {
            usePopulation = options.getBoolean("noveltyUsePopulation");
        }
        if (options.containsKey("noveltyUseRepository")) {
            useRepository = options.getBoolean("noveltyUseRepository");
        }
        if (options.containsKey("repositoryRandomCriteria")) {
            randomCriteria = options.getBoolean("repositoryRandomCriteria");
        }
        if (options.containsKey("repositorySizeLimit")) {
            repoSizeLimit = options.getInteger("repositorySizeLimit");
        }


        if (options.containsKey("mixer")) {
            String s = options.get("mixer");
            try {
                mixer = (NoveltyFitnessMixer) ComponentLoader.createInstance(NoveltyFitnessMixer.class, s);
                mixer.init(main);
            } catch (Exception ex) {
                Logger.getLogger(BaseNoveltyImplementation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /*
         * Prepare logs
         */
        main.getLogger().appendLine("repoLog",
                "Generation", "Most novel", "Avg Novelty", "Least novel",
                "Repo size", "Best in repo", "Max repo comps", "Avg repo comps", "Min repo comps",
                "Best repo fit", "Avg repo fit", "Worst repo fit", "Threshold",
                "Max distance", "Avg distance", "Min distance");
        main.getLogger().appendLine("repository",
                "Order", "Generation", "Fitness", "Number neurons", "Number links");

        /*
         * Continue previous evolution
         */
        if (main.getExtraOptions().containsKey("continue")) {
            File folder = new File(main.getExtraOptions().get("continue"));
            try {
                ObjectInputStream in = new ObjectInputStream(
                        new BufferedInputStream(
                        new FileInputStream(
                        new File(folder, "genomeRepository.ser"))));
                noveltyRepository = (HashMap<Chromosome, Pair<Double, C>>) in.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(NEATEvolution.class.getName()).log(Level.SEVERE, null, ex);
            }

            File log = new File(folder, "repoLog.csv");
            try {
                CSVReader reader = new CSVReader(new FileReader(log), '\t', '"');
                List<String[]> all = reader.readAll();
                String[] last = all.get(all.size() - 1);
                double newT = Double.parseDouble(last[last.length - 1]);
                System.out.println("Restoring threshold " + newT);
                this.threshold = newT;
            } catch (IOException ex) {
                Logger.getLogger(NoveltyNEAT.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void evolutionIteration() {
        /*
         * Iteration & threshold adjust
         */
        if (randomCriteria) {
            super.evolutionIteration();
        } else {
            int deltaSize = noveltyRepository.size();
            super.evolutionIteration(); // HERE!!
            deltaSize = noveltyRepository.size() - deltaSize;
            if (deltaSize > maximumAdded) {
                threshold *= (1 + thresholdAdjust);
            } else if (deltaSize < minimumAdded) {
                threshold *= (1 - thresholdAdjust);
            }
        }

        /*
         * Logging
         */
        int bestInRepo = noveltyRepository.containsKey(fitnessStats.bestChromosome) ? 1 : 0;
        Object[] repoStats = new Object[]{
            trainIteration,
            noveltyStats.bestScore,
            noveltyStats.getAverageScore(),
            noveltyStats.worstScore,
            noveltyRepository.size(),
            bestInRepo,
            repoComparisionStats.bestScore,
            repoComparisionStats.getAverageScore(),
            repoComparisionStats.worstScore,
            repositoryFitnessStats.bestScore,
            repositoryFitnessStats.getAverageScore(),
            repositoryFitnessStats.worstScore,
            threshold,
            distanceStats.bestScore,
            distanceStats.getAverageScore(),
            distanceStats.worstScore
        };

        main.getLogger().appendLine("repoLog", repoStats);
        main.getLogger().saveObject(noveltyRepository, "genomeRepository.ser");

        notifyStatus(trainIteration + "\t" + noveltyRepository.size() + "\t"
                + scoreStats.bestScore + "\t" + scoreStats.getAverageScore() + "\t" + fitnessStats.bestScore);
    }

    @Override
    protected Map<Chromosome, double[]> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        this.populationBehaviours = evaluatePopulationBehaviours(evaluations);
        Map<Chromosome, Double> noveltyScores = new HashMap<>(evaluations.size() * 2);
        for (Chromosome genome : evaluations.keySet()) {
            ArrayList<Pair<Double, Boolean>> dists = new ArrayList<>(populationBehaviours.size() + noveltyRepository.size());

            // Measure distances to the rest of the population
            if (usePopulation) {
                List<Double> distsToPop = distancesToPopulation(genome);
                for (Double d : distsToPop) {
                    dists.add(Pair.of(d, false));
                }
            }

            // Measure distances to the repository
            if (useRepository) {
                List<Double> distsToRepo = distancesToRepository(genome);
                updateRepository(genome, distsToRepo);
                for (Double d : distsToRepo) {
                    dists.add(Pair.of(d, true));
                }
            }

            // Novelty measure - average distance to K closest individuals
            Collections.sort(dists, new Comparator<Pair<Double, Boolean>>() {
                @Override
                public int compare(Pair<Double, Boolean> d1, Pair<Double, Boolean> d2) {
                    return new Double(d1.getLeft()).compareTo(d2.getLeft());
                }
            });
            double noveltyMeasure = 0;
            int fromRepository = 0;
            int nClosest = Math.min(k, dists.size());
            for (int i = 0; i < nClosest; i++) {
                Pair<Double, Boolean> p = dists.get(i);
                noveltyMeasure += p.getLeft();
                if (p.getRight()) {
                    fromRepository++;
                }
            }
            noveltyMeasure /= nClosest;
            repoComparisionStats.process(genome, fromRepository);
            noveltyStats.process(genome, noveltyMeasure);

            noveltyScores.put(genome, noveltyMeasure);
        }

        if (mixer != null) {
            return mixer.mix(noveltyScores, fitnessScores);
        } else {
            HashMap<Chromosome, double[]> res = new HashMap<>(noveltyScores.size() * 2);
            for (Entry<Chromosome, Double> e : noveltyScores.entrySet()) {
                res.put(e.getKey(), new double[]{e.getValue()});
            }
            return res;
        }
    }

    protected HashMap<Chromosome, C> evaluatePopulationBehaviours(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        // Average the behaviors
        repoComparisionStats = new EvolutionMethod.Stats();
        noveltyStats = new EvolutionMethod.Stats();
        distanceStats = new EvolutionMethod.Stats();
        populationBehaviours = new HashMap<>();
        for (Map.Entry<Chromosome, List<EvaluationFunction>> e : evaluations.entrySet()) {
            populationBehaviours.put(e.getKey(), produceBehaviourDescription(e.getKey(), e.getValue()));
        }

        return populationBehaviours;
    }

    protected abstract C produceBehaviourDescription(Chromosome c, List<EvaluationFunction> evals);

    protected List<Double> distancesToPopulation(Chromosome c) {
        return distancesToBehaviours(populationBehaviours.get(c), populationBehaviours.values());
    }

    protected List<Double> distancesToRepository(Chromosome c) {
        ArrayList<C> repoBehavs = new ArrayList<>(noveltyRepository.size());
        for (Pair<Double, C> p : noveltyRepository.values()) {
            repoBehavs.add(p.getRight());
        }
        List<Double> distsToRepo = Collections.EMPTY_LIST;
        if (!repoBehavs.isEmpty()) {
            distsToRepo = distancesToBehaviours(populationBehaviours.get(c), repoBehavs);
        }
        return distsToRepo;
    }

    protected List<Double> distancesToBehaviours(C behav, Collection<C> all) {
        List<Double> dists = new ArrayList<>(all.size());
        for (C charac : all) {
            if (behav != charac) {
                double dist = behaviourDistanceMeasure(behav, charac);
                distanceStats.process(null, dist);
                dists.add(dist);
            }
        }
        return dists;
    }

    protected abstract double behaviourDistanceMeasure(C behav1, C behav2);

    protected void updateRepository(Chromosome c, List<Double> distancesToRepo) {
        if (randomCriteria) {
            double rand = Math.random();
            if (rand < threshold) {
                addToRepository(c, 0);
            }
        } else {
            // Average distance to the K closest individuals of the repository
            Collections.sort(distancesToRepo);
            double repoNovelty = 0;
            for (int i = 0; i < k && i < distancesToRepo.size(); i++) {
                repoNovelty += distancesToRepo.get(i) / Math.min(k, distancesToRepo.size());
            }

            // If the individual should go to the repository
            if (noveltyRepository.isEmpty() || repoNovelty > threshold) {
                addToRepository(c, repoNovelty);
            }
        }
    }

    protected void addToRepository(Chromosome c, double novelty) {
        // Check if size is reached
        if (noveltyRepository.size() >= repoSizeLimit) {
            ArrayList<Chromosome> keys = new ArrayList<>(noveltyRepository.keySet());
            Random random = new Random();
            Chromosome randomKey = keys.get(random.nextInt(noveltyRepository.size()));
            noveltyRepository.remove(randomKey);
        }

        // Add to the repository
        Pair<Double, C> repoElement = Pair.of(fitnessScores.get(c), populationBehaviours.get(c));
        noveltyRepository.put(c, repoElement);
        repoIndividualId++;

        // Update repository stats
        repositoryFitnessStats.process(c, fitnessScores.get(c));

        // Persist repository individual
        NEATNeuralNet net = decodeChromosome(c);
        main.getLogger().saveIndividual(net, "repo/repo" + repoIndividualId);

        // Log the individual
        int links = 0;
        for (NEATNeuron neuron : net.neurons()) {
            links += neuron.incomingSynapses().size();
        }
        main.getLogger().appendLine("repository", repoIndividualId, trainIteration, repoElement.getLeft(), net.neurons().length, links);
        notifyStatus("Added to repo (" + repoIndividualId + "): " + novelty);
    }
}
