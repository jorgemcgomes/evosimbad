/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

import evosimbad.core.Simulation;
import evosimbad.evolution.InformedCharacterisation;

/**
 *
 * @author Jorge
 */
public class AggregationClustersBehaviour extends InformedCharacterisation {

    protected AggregationCenterMass centerMass;
    protected AggregationClusters2 clusters;

    public AggregationClustersBehaviour(int updateRate, double clusterThreshold) {
        super(updateRate);
        centerMass = new AggregationCenterMass(updateRate);
        clusters = new AggregationClusters2(updateRate, clusterThreshold);
    }

    @Override
    public void setSimulatorExperiment(Simulation exp) {
        super.setSimulatorExperiment(exp);
        centerMass.setSimulatorExperiment(exp);
        clusters.setSimulatorExperiment(exp);
    }

    @Override
    public void step() {
        super.step();
        centerMass.step();
        clusters.step();
    }

    @Override
    public void evaluate() {
        ; // nothind to do here, evaluate is also called in the center of mass and clusters functions
    }

    @Override
    public double getFitness() {
        return centerMass.getFitness();
    }

    @Override
    public double[] getBehaviour() {
        return clusters.getBehaviour();
    }
}
