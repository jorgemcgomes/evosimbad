/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.core.ExtraOptions;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author Jorge
 */
public class NoveltyNEAT extends BaseNoveltyImplementation<Double[]> {

    protected boolean savePopulation = false;

    public NoveltyNEAT(int numberGenerations, int k, double tInitial) {
        super(numberGenerations, k, tInitial);
    }

    /*
     * The following methods are overrided just for logging
     */
    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        ExtraOptions options = main.getExtraOptions();
        if (options.containsKey("savePopulation")) {
            savePopulation = options.getBoolean("savePopulation");
            if (savePopulation) {
                main.getLogger().appendLine("population", "Generation", "Fitness");
            }
        }
    }

    @Override
    public void evolutionIteration() {
        super.evolutionIteration();
        if (savePopulation) {
            for (Entry<Chromosome, Double[]> ind : populationBehaviours.entrySet()) {
                main.getLogger().appendLine("population", ArrayUtils.addAll(
                        new Object[]{(Integer) trainIteration, (Double) fitnessScores.get(ind.getKey())},
                        (Object[]) (ind.getValue())));
            }
        }
    }

    @Override
    protected void addToRepository(Chromosome c, double novelty) {
        super.addToRepository(c, novelty);
        main.getLogger().appendLine("repoIndividuals", (Object[]) populationBehaviours.get(c));
    }

    /*
     * The juice is from now on
     */
    @Override
    protected Double[] produceBehaviourDescription(Chromosome c, List<EvaluationFunction> evals) {
        Double[] averageBehaviour = null;
        for (EvaluationFunction eval : evals) {
            InformedCharacterisation noveltyEval = (InformedCharacterisation) eval;
            double[] behaviour = noveltyEval.getBehaviour();
            if (averageBehaviour == null) {
                averageBehaviour = new Double[behaviour.length];
                Arrays.fill(averageBehaviour, new Double(0));
            }
            for (int i = 0; i < behaviour.length; i++) {
                averageBehaviour[i] += behaviour[i] / evals.size();
            }
        }
        return averageBehaviour;
    }

    @Override
    protected double behaviourDistanceMeasure(Double[] behav1, Double[] behav2) {
        double sum = 0;
        for (int i = 0; i < behav1.length; i++) {
            sum += Math.pow(behav1[i] - behav2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
