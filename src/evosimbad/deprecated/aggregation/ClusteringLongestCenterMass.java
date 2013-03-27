/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

import evosimbad.core.Simulation;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Jorge
 */
public class ClusteringLongestCenterMass extends ClusteringLongest {

    protected AggregationCenterMass centerMass;
    
    public ClusteringLongestCenterMass(int updateRate, double threshold, int minDuration) {
        super(updateRate, threshold, minDuration);
        centerMass = new AggregationCenterMass(updateRate);
    }

    @Override
    public double[] getBehaviour() {
        double[] clustersBehav = super.getBehaviour();
        double[] centerMassBehav = centerMass.getBehaviour();
        return ArrayUtils.addAll(clustersBehav, centerMassBehav);
    }

    @Override
    public void setSimulatorExperiment(Simulation exp) {
        super.setSimulatorExperiment(exp);
        centerMass.setSimulatorExperiment(exp);
    }

    @Override
    public void step() {
        super.step();
        centerMass.step();
    }
}
