/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import java.util.HashMap;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class MultiObjectiveMixer extends NoveltyFitnessMixer {

    @Override
    protected Map<Chromosome, double[]> mix(Map<Chromosome, Double> noveltyScores, Map<Chromosome, Double> fitnessScores) {
        Map<Chromosome, double[]> res = new HashMap<>(noveltyScores.size() * 2);
        for(Chromosome c : noveltyScores.keySet()) {
            res.put(c, new double[]{noveltyScores.get(c), fitnessScores.get(c)});
        }
        return res;
    }
    
}
