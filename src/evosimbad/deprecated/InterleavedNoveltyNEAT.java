/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated;

import evosimbad.core.EvaluationFunction;
import evosimbad.evolution.StandardizedNoveltyNEAT;
import java.util.List;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class InterleavedNoveltyNEAT extends StandardizedNoveltyNEAT {

    private int interval;
    private boolean noveltyMode = true;
    
    public InterleavedNoveltyNEAT(int numberGenerations, int k, double tInitial, int interval) {
        super(numberGenerations, k, tInitial);
        this.interval = interval;
    }

    @Override
    protected Map<Chromosome, Double> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        Map<Chromosome, Double> noveltyScores = super.calculateScores(evaluations); // repository stuff
        if(super.trainIteration % interval == 0) {
            noveltyMode = !noveltyMode;
            System.out.println("Switching to : " + (noveltyMode ? "Novelty" : "Fitness"));
        }
        
        if(noveltyMode) {
            return noveltyScores;
        } else {
            return fitnessScores;
        }
    }
}
