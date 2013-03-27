/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation.test;

import evosimbad.evolution.NoveltyFunction;

/**
 *
 * @author Jorge
 */
public class AggregationEvaluation3 extends NoveltyFunction {
    
    private double fitness = 0;
    private double fitnessWCount = 0;
    
    private double[] behaviour = null;
    private int index = 0;
    
    public AggregationEvaluation3(int updateRate) {
        super(updateRate);
    }

    @Override
    public void evaluate() {
        // Fitness - media ponderada, quanto mais para a frente mais importancia tem
        double dist = CenterMassEvaluation.averageDistanceToCenterMass(experiment.getEnvironment().getAgents());
        double w = experiment.getCurrentStep() / (double) experiment.getTotalSteps();
        fitnessWCount += w;
        fitness += dist * w;
        
        // Behaviour - numero de clusters
        if(behaviour == null) {
            behaviour = new double[getTotalEvaluations()];
        }
        behaviour[index++] = dist;
    } 

    @Override
    public double[] getBehaviour() {
        return behaviour;
    }

    @Override
    public double getFitness() {
        return - fitness / fitnessWCount;
    }
}
