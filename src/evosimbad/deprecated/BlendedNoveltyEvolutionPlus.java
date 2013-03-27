/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated;

import evosimbad.core.EvaluationFunction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author Jorge
 */
public class BlendedNoveltyEvolutionPlus extends FitnessBehaviourNoveltyNEAT {

    private double noveltyRatio;
    
    /**
     * @param numberGenerations
     * @param populationSize
     * @param k
     * @param tInitial
     * @param noveltyRatio Must be between 0 (only fitness) and 1 (only novelty)
     */
    public BlendedNoveltyEvolutionPlus(int numberGenerations, int k, double tInitial, double noveltyRatio) {
        super(numberGenerations, k, tInitial);
        this.noveltyRatio = noveltyRatio;
    }

    @Override
    protected Map<Chromosome, Double> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        Map<Chromosome, Double> nov = super.calculateScores(evaluations);
        Map<Chromosome, Double> fit = super.calculateFitnessScores(evaluations);
        
        double noveltyMin = Double.POSITIVE_INFINITY, noveltyMax = Double.NEGATIVE_INFINITY;
        for(double v : nov.values()) {
            noveltyMin = Math.min(noveltyMin, v);
            noveltyMax = Math.max(noveltyMax, v);
        }
        
        double fitnessMin = Double.POSITIVE_INFINITY, fitnessMax = Double.NEGATIVE_INFINITY;
        for(double v : fit.values()) {
            fitnessMin = Math.min(fitnessMin, v);
            fitnessMax = Math.max(fitnessMax, v);
        }
        
        HashMap<Chromosome, Double> score = new HashMap<>(evaluations.size() * 2);
        for(Chromosome g : evaluations.keySet()) {
            double fitNorm = fitnessMax == fitnessMin ? 0 : (fit.get(g) - fitnessMin) / (fitnessMax - fitnessMin);
            double novNorm = noveltyMax == noveltyMin ? 0 : (nov.get(g) - noveltyMin) / (noveltyMax - noveltyMin);
            double s = (1 - noveltyRatio) * fitNorm + noveltyRatio * novNorm;
            score.put(g, s);
        }
        
        return score;
    }
}
