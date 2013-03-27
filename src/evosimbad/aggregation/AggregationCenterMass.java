/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation;

import evosimbad.evolution.InformedCharacterisation;

/**
 *
 * @author jorge
 */
public class AggregationCenterMass extends InformedCharacterisation {

    private double[] behaviour;
    private int index = 0;
    private double normalizationConstant = -1;

    public AggregationCenterMass(int updateRate) {
        super(updateRate);
    }

    @Override
    public double[] getBehaviour() {
        return behaviour;
    }

    @Override
    public void evaluate() {
        if (normalizationConstant == -1) { // normalizes between 0 and 1
            double size = experiment.getEnvironment().worldSize;
            normalizationConstant = 1 / (Math.sqrt(size * size * 2) / 2);
        }
        if (behaviour == null) {
            behaviour = new double[getTotalEvaluations()];
        }
        double dist = AggregationFitness.averageDistanceToCenterMass(experiment.getEnvironment().getAgents()) * normalizationConstant;
        behaviour[index++] = dist;
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
