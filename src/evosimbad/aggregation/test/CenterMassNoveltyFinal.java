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
public class CenterMassNoveltyFinal extends NoveltyFunction {
    
    private double[] behaviour;
    private int index = 0;
    
    public CenterMassNoveltyFinal(int noveltySampleRate, int fitnessUpdateRate, int evaluatedSteps) {
        super(noveltySampleRate, new CenterMassFinal(fitnessUpdateRate, evaluatedSteps));
    }

    @Override
    public void evaluate() {
        if(behaviour == null) {
            behaviour = new double[super.getTotalEvaluations()];
        }
        double avgDist = CenterMassEvaluation.averageDistanceToCenterMass(experiment.getEnvironment().getAgents());
        behaviour[index++] = avgDist;
    }
    
    @Override
    public double[] getBehaviour() {
        return behaviour;
    }      
}
