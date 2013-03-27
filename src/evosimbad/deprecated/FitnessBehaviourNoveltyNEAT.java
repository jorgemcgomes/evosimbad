/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated;

import evosimbad.core.EvaluationFunction;
import evosimbad.evolution.StandardizedNoveltyNEAT;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class FitnessBehaviourNoveltyNEAT extends StandardizedNoveltyNEAT {
   
    public FitnessBehaviourNoveltyNEAT(int numberGenerations, int k, double tInitial) {
        super(numberGenerations, k, tInitial);
    }

    @Override
    protected double[] produceBehaviourDescription(Chromosome c, List<EvaluationFunction> evals) {
        double[] bDescr = super.produceBehaviourDescription(c, evals);
        return ArrayUtils.add(bDescr, fitnessScores.get(c));
    }

    /*
     * O que se pretende: behaviours iguais mas fitnesses diferentes devem resultar numa grande distancia
     * behaviours diferentes mas com fitnesses iguais nao devem resultar em menores distancias
     * 
     * fitness dist = Fd ; behaviour dist = Bb
     * Big Fd + Big Bd = Bd
     * Small Fd + Big Bd = Bd
     * Small Fd + Small Bd = Bd
     * Big Fd + Small Bd = Fd
     * 
     *     | Fd
     * Bd  | 0    0.5    1     10
     * ---------------------------
     * 0   | 0    0.25   0.5   5
     * 0.5 | 0.5  0.5    0.75  5.25
     * 1   | 1    1      1     5.5
     * 10  | 10   10     10    10
     */
    @Override
    protected double behaviourDistanceMeasure(double[] behav1, double[] behav2) {
        // remove the fitness in last position to calculate the behaviour distance
        double[] b1 = new double[behav1.length - 1];
        System.arraycopy(behav1, 0, b1, 0, b1.length);
        double[] b2 = new double[behav2.length - 1];
        System.arraycopy(behav2, 0, b2, 0, b2.length);
        double behaviourDistance = super.behaviourDistanceMeasure(b1, b2);
        
        // fitness distance
        double fitnessDistance = Math.abs(behav1[behav1.length - 1] - behav2[behav2.length - 1]);
        
        // combine -- see table above
        //double combined = Math.min(behaviourDistance, (behaviourDistance + fitnessDistance) / 2);
        double combined = behaviourDistance + fitnessDistance;
        
        return combined;
    }
}
