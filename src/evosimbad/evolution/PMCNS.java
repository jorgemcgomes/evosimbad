/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.ComponentLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class PMCNS extends NoveltyFitnessMixer {

    public static final double NULLIFY_FACTOR = 0.01;
    private double percentile; // 0 (all meet minimalCriteria) - 1 (no one meets)
    private double smoothing; // 0 (no adjust) - 1 (immediate adjust)
    private double minimalCriteria = 0;
    private int aptCount = 0;

    public PMCNS(double percentile, double adjust) {
        this.percentile = percentile;
        this.smoothing = adjust;
    }

    @Override
    public void init(ComponentLoader main) {
        super.init(main);
        main.getLogger().appendLine("mcnsLog", "Generation", "Minimal Criteria", "Apt count");
    }

    @Override
    protected Map<Chromosome, double[]> mix(Map<Chromosome, Double> noveltyScores, Map<Chromosome, Double> fitnessScores) {
        ArrayList<Double> fitnessValues = new ArrayList<>(fitnessScores.values());
        Collections.sort(fitnessValues);
        int cutIndex = (int) (percentile * fitnessValues.size());
        double currentCut = fitnessValues.get(cutIndex);
        minimalCriteria += Math.max(0, (currentCut - minimalCriteria) * smoothing); // no decreases allowed

        Map<Chromosome, double[]> mcScores = new HashMap<>(noveltyScores.size() * 2);
        aptCount = 0;
        for (Chromosome c : noveltyScores.keySet()) {
            double novScore = noveltyScores.get(c);
            if (fitnessScores.get(c) >= minimalCriteria) {
                aptCount++;
                mcScores.put(c, new double[]{novScore});
            } else {
                mcScores.put(c, new double[]{novScore * NULLIFY_FACTOR});
            }
        }

        /*
         * Logging
         */
        main.getLogger().appendLine("mcnsLog", main.getEvolutionMethod().getTrainIteration(), minimalCriteria, aptCount);

        return mcScores;
    }
}
