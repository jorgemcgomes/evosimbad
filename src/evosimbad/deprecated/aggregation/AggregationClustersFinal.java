/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

/**
 *
 * @author Jorge
 */
public class AggregationClustersFinal extends AggregationClusters {

    public AggregationClustersFinal(int updateRate, double threshold) {
        super(updateRate, threshold);
    }

    @Override
    public double getFitness() {
        int finalClusters = -1;
        int permanenceTime = 0;
        for(int i = clustersEvolution.length - 1 ; i >= 0 ; i--) { // ISTO PODE NAO ESTAR A FUNCAR BEM!!!!!!!
            if(finalClusters == -1) {
                finalClusters = clustersEvolution[i];
            } else if(finalClusters != clustersEvolution[i]) {
                break;
            }
            permanenceTime++;
        }
        
        return 1 - (finalClusters - permanenceTime / (double) getTotalEvaluations()) / (maxAgents - 1);
    }  
}
