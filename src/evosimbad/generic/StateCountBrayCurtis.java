/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class StateCountBrayCurtis extends StateCountNovelty {

    private Stats brayCurtis;

    public StateCountBrayCurtis(int numberGenerations, int k, double tInitial, double cutoff) {
        super(numberGenerations, k, tInitial, cutoff);
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        main.getLogger().appendLine("brayCurtisStats",
                "Iteration",
                "Greatest distance",
                "Average distance",
                "Lowest distance");
    }

    @Override
    protected Map<Chromosome, double[]> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        brayCurtis = new Stats();
        Map<Chromosome, double[]> s = super.calculateScores(evaluations);
        main.getLogger().appendLine("brayCurtisStats",
                trainIteration,
                brayCurtis.bestScore,
                brayCurtis.getAverageScore(),
                brayCurtis.worstScore);
        return s;
    }

    @Override
    protected double behaviourDistanceMeasure(StateCount sc1, StateCount sc2) {
        HashMap<Integer, Float> m1 = sc1.getCountMap();
        HashMap<Integer, Float> m2 = sc2.getCountMap();

        int commonCount = 0, uniqueCount = 0;
        double dist = 0;
        for (Integer i : m1.keySet()) {
            if (m2.containsKey(i)) { // os que estao em ambos
                dist += Math.abs(m1.get(i) - m2.get(i));
                commonCount++;
            } else { // os que estao apenas em m1
                dist += m1.get(i);
                uniqueCount++;
            }
        }
        for (Integer i : m2.keySet()) {
            if (!m1.containsKey(i)) { // nao estao em m1 - logo estao apenas em m2
                dist += m2.get(i);
                uniqueCount++;
            }
        }
        commonPatternStats.process(null, commonCount);
        uniquePatternStats.process(null, uniqueCount);
        commonRatioStats.process(null, (double) commonCount / Math.min(m1.size(), m2.size()));
        
        // actual distance calculation
        float total1 = 0;
        for(Float f : m1.values()) {
            total1 += f;
        }
        float total2= 0;
        for(Float f : m2.values()) {
            total2 += f;
        }
        
        dist = dist / (total1 + total2);
        brayCurtis.process(null, dist);
        
        return dist;
    }
}
