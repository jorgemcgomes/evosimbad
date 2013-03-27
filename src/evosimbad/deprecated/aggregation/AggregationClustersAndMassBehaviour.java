/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Fitness = center of mass
 * Behaviour = number of clusters + center of mass
 * @author Jorge
 */
public class AggregationClustersAndMassBehaviour extends AggregationClustersBehaviour {

    public AggregationClustersAndMassBehaviour(int updateRate, double clusterThreshold) {
        super(updateRate, clusterThreshold);
    }

    @Override
    public double[] getBehaviour() {
        double[] massBehaviour = centerMass.getBehaviour();
        double min = 0.15, max = 0.75;
        for(int i = 0 ; i < massBehaviour.length ; i++) {
            massBehaviour[i] = Math.max(Math.min((massBehaviour[i] - min) / (max - min), 1), 0);
        }
        double[] clustersBehaviour = clusters.getBehaviour();
        return ArrayUtils.addAll(clustersBehaviour, massBehaviour);
    }
}
