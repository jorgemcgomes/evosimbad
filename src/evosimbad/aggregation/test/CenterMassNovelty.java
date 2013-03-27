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
public class CenterMassNovelty extends NoveltyFunction {
    
    private double[] behaviour;
    private int index = 0;
    private double fitness = 0;
    
    public CenterMassNovelty(int updateRate) {
        super(updateRate);
    }

    @Override
    public void evaluate() {
        if(behaviour == null) {
            behaviour = new double[super.getTotalEvaluations()];
        }
        double avgDist = CenterMassEvaluation.averageDistanceToCenterMass(experiment.getEnvironment().getAgents());
        behaviour[index++] = avgDist;
        fitness += avgDist;
    }

    @Override
    public double getFitness() {
        return - fitness / super.getTotalEvaluations();
    }
    
    @Override
    public double[] getBehaviour() {
        return behaviour;
    }        
}
