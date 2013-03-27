/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.EvaluationFunction;
import java.util.List;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class DynamicInterleavingNovelty extends StandardizedNoveltyNEAT {

    public DynamicInterleavingNovelty(int numberGenerations, int k, double tInitial) {
        super(numberGenerations, k, tInitial);
    }

    @Override
    protected Map<Chromosome, Double> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        Map<Chromosome, Double> noveltyScores = super.calculateScores(evaluations);
        
                
    }
    
    private double populationDispersion() {
        
        return 0;
    }
}
