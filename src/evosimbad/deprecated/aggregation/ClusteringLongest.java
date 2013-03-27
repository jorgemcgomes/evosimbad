/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

/**
 *
 * @author Jorge
 */
public class ClusteringLongest extends AggregationClusters {

    private int minDuration;

    public ClusteringLongest(int updateRate, double threshold, int minDuration) {
        super(updateRate, threshold);
        this.minDuration = minDuration;
    }

    @Override
    public double getFitness() {
        int longest = 0, minClusters = Integer.MAX_VALUE;
        int tempCount = 1;
        for (int i = 1; i < clustersEvolution.length; i++) {
            if (clustersEvolution[i] == clustersEvolution[i - 1]) { // sequence continuation
                tempCount++;
            } else { // sequence change
                if (tempCount >= minDuration
                        && (clustersEvolution[i - 1] < minClusters
                        || (clustersEvolution[i - 1] == minClusters && tempCount > longest))) {
                    longest = tempCount;
                    minClusters = clustersEvolution[i - 1];
                }
                tempCount = 1;
            }
        }
        // check ending sequence
        int lastClusters = clustersEvolution[clustersEvolution.length-1];
        if (tempCount >= minDuration
                && (lastClusters < minClusters
                || (lastClusters == minClusters && tempCount > longest))) {
            longest = tempCount;
            minClusters = lastClusters;
        }
        
        if(longest == 0) { // not found any sequence longer than minDuration
            return 0;
        }
        return 1 - (minClusters - longest / (double) getTotalEvaluations()) / maxAgents;
    }
}
