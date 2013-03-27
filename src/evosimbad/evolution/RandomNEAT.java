/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.EvaluationFunction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author Jorge
 */
public class RandomNEAT extends NEATEvolution {
    
    public RandomNEAT(int numberGenerations) {
        super(numberGenerations);
    }

    @Override
    public void evolutionIteration() {
        super.evolutionIteration();
        notifyStatus(trainIteration + "\t" + fitnessStats.bestScore + "\t" + fitnessStats.getAverageScore());
    }

    @Override
    protected Map<Chromosome, double[]> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        Map<Chromosome, double[]> sc = new HashMap<>(evaluations.size() * 2);
        for(Chromosome g : evaluations.keySet()) {
            sc.put(g, new double[]{Math.random()});
        }
        return sc;
    }
}
