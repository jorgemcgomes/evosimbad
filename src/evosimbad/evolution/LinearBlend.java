/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.ComponentLoader;
import java.util.HashMap;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class LinearBlend extends NoveltyFitnessMixer {

    private double noveltyRatio;

    public LinearBlend(double noveltyRatio) {
        this.noveltyRatio = noveltyRatio;
    }

    @Override
    protected Map<Chromosome, double[]> mix(Map<Chromosome, Double> noveltyScores, Map<Chromosome, Double> fitnessScores) {
        double noveltyMin = Double.POSITIVE_INFINITY, noveltyMax = Double.NEGATIVE_INFINITY;
        for (double v : noveltyScores.values()) {
            noveltyMin = Math.min(noveltyMin, v);
            noveltyMax = Math.max(noveltyMax, v);
        }

        double fitnessMin = Double.POSITIVE_INFINITY, fitnessMax = Double.NEGATIVE_INFINITY;
        for (double v : fitnessScores.values()) {
            fitnessMin = Math.min(fitnessMin, v);
            fitnessMax = Math.max(fitnessMax, v);
        }

        HashMap<Chromosome, double[]> score = new HashMap<>(noveltyScores.size() * 2);
        for (Chromosome g : noveltyScores.keySet()) {
            double fitNorm = fitnessMax == fitnessMin ? 0 : (fitnessScores.get(g) - fitnessMin) / (fitnessMax - fitnessMin);
            double novNorm = noveltyMax == noveltyMin ? 0 : (noveltyScores.get(g) - noveltyMin) / (noveltyMax - noveltyMin);
            double s = (1 - noveltyRatio) * fitNorm + noveltyRatio * novNorm;
            score.put(g, new double[]{s});
        }

        return score;
    }
}
