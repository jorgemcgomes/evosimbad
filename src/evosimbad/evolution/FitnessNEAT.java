/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 *
 * @author Jorge
 */
public class FitnessNEAT extends NEATEvolution {

    protected boolean savePopulation;
    protected double savePopulationSample = 0;
    protected int repoId = 1;

    public FitnessNEAT(int numberGenerations) {
        super(numberGenerations);
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        if (main.getExtraOptions().containsKey("savePopulation")) {
            this.savePopulation = main.getExtraOptions().getBoolean("savePopulation");
        }
        if(main.getExtraOptions().containsKey("populationSample")) {
            this.savePopulationSample = main.getExtraOptions().getDouble("populationSample");
        }
        
        if (savePopulation) {
            main.getLogger().appendLine("population", "Generation", "Fitness");
        }
        
    }

    @Override
    public void evolutionIteration() {
        super.evolutionIteration();
        /*
         * Logging
         */
        if (savePopulation) {
            // Average the behaviors
            for (Map.Entry<Chromosome, List<EvaluationFunction>> e : experimentResults.entrySet()) {
                double[] averageBehaviour = null;
                for (EvaluationFunction eval : e.getValue()) {
                    InformedCharacterisation noveltyEval = (InformedCharacterisation) eval;
                    double[] behaviour = noveltyEval.getBehaviour();
                    if (averageBehaviour == null) {
                        averageBehaviour = Arrays.copyOf(behaviour, behaviour.length);
                    } else {
                        for (int i = 0; i < behaviour.length; i++) {
                            averageBehaviour[i] += behaviour[i];
                        }
                    }
                }
                for (int i = 0; i < averageBehaviour.length; i++) {
                    averageBehaviour[i] /= e.getValue().size();
                }
                Object[] line = (Object[]) ArrayUtils.addAll(
                        new Object[]{(Integer) trainIteration, (Double) fitnessScores.get(e.getKey())},
                        (Object[]) ArrayUtils.toObject(averageBehaviour));
                main.getLogger().appendLine("population", line);
            }
        }
        notifyStatus(trainIteration + "\t" + fitnessStats.bestScore + "\t" + fitnessStats.getAverageScore());
    }

    @Override
    protected Map<Chromosome, double[]> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        HashMap<Chromosome, double[]> res = new HashMap<>(fitnessScores.size() * 2);
        for (Map.Entry<Chromosome, Double> e : fitnessScores.entrySet()) {
            res.put(e.getKey(), new double[]{e.getValue()});
            // log population sample
            if(savePopulationSample > 0 && Math.random() < savePopulationSample) {
                NeuralNet net = decodeChromosome(e.getKey());
                main.getLogger().saveIndividual(net, "repo/repo" + repoId++);
            }
        }
        return res;
    }
}
