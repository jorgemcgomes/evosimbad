/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation;

import evosimbad.core.EvaluationFunction;

/**
 *
 * @author Jorge
 */
public class AggregationFitnessFinal extends EvaluationFunction {
    
    private double fitness;

    @Override
    public void evaluate() {
        if(getCurrentEvaluation() == getTotalEvaluations()) {
            double size = experiment.getEnvironment().worldSize;
            double norm = 1 / (Math.sqrt(size * size * 2) / 2);
            double dist = AggregationFitness.averageDistanceToCenterMass(experiment.getEnvironment().getAgents());
            fitness = dist * norm;
        }
    }

    @Override
    public double getFitness() {
        return 1 - fitness;
    }
    
}
