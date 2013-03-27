/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.evolution.StandardizedNoveltyNEAT;
import java.util.*;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class FixedMinimalCriteriaNoveltyNEAT extends StandardizedNoveltyNEAT {

    public static final double NULLIFY_FACTOR = 0.00;
    private double minimalCriteria = 0;
    private Map<Chromosome, Double> mcScores;
    private int aptCount = 0;

    public FixedMinimalCriteriaNoveltyNEAT(int numberGenerations, int k, double tInitial, double criteria) {
        super(numberGenerations, k, tInitial);
        this.minimalCriteria = criteria;
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        main.getLogger().appendLine(expName + "/mcnsLog",
                "Generation", "Minimal Criteria", "Apt count");
    }
    
    @Override
    public void evolutionIteration() {
        super.evolutionIteration();
        main.getLogger().appendLine(expName + "/mcnsLog", trainIteration, minimalCriteria, aptCount);
    }

    @Override
    protected Map<Chromosome, Double> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        Map<Chromosome, Double> noveltyScores = super.calculateScores(evaluations);
        mcScores = new HashMap<>(noveltyScores.size() * 2);
        aptCount = 0;
        for (Chromosome c : noveltyScores.keySet()) {
            double novScore = noveltyScores.get(c);
            if (fitnessScores.get(c) >= minimalCriteria) {
                aptCount++;
                mcScores.put(c, novScore);
            } else {
                mcScores.put(c, novScore * NULLIFY_FACTOR);
            }
        }
        return mcScores;
    }
}

    

