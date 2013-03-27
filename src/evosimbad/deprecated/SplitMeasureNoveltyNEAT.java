/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated;

import evosimbad.evolution.AggregateNoveltyFunction;
import evosimbad.evolution.NoveltyNEAT;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author jorge
 */
public class SplitMeasureNoveltyNEAT extends NoveltyNEAT {

    public SplitMeasureNoveltyNEAT(int numberGenerations, int k, double tInitial) {
        super(numberGenerations, k, tInitial);
    }

    @Override
    protected List<Double> distancesToBehaviours(double[] behav, Collection<double[]> all) {
        if (sampleFunction instanceof AggregateNoveltyFunction) {
            AggregateNoveltyFunction fun = (AggregateNoveltyFunction) sampleFunction;
            int[] splits = fun.splitIndexes();
            List<Double>[] partDistances = new ArrayList[splits.length];
            for (int i = 0; i < splits.length; i++) {
                // build a new list with only this behaviour segment
                int from = splits[i];
                int to = i == splits.length - 1 ? behav.length : splits[i + 1];
                double[] behavPart = extract(behav, from, to);
                ArrayList<double[]> allParts = new ArrayList<>();
                for (double[] b : all) {
                    if (behav != b) {
                        allParts.add(extract(b, from, to));
                    }
                }
                
                // calculate the distances between this behaviour and the others - respective to this segment
                List<Double> dists = super.distancesToBehaviours(behavPart, allParts);
                
                // normalize the distances
                double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
                for(Double d : dists) {
                    min = Math.min(min, d);
                    max = Math.max(max, d);
                }
                List<Double> normalizedDistances = new ArrayList<>(dists.size());
                for(Double d : dists) {
                    normalizedDistances.add(max == min ? 0 : (d - min) / (max - min));
                }                
                partDistances[i] = normalizedDistances;
            }
            
            // combine the distances from the different behaviour segments
            List<Double> dists = new ArrayList<>(partDistances[0].size());
            for(int i = 0 ; i < partDistances[0].size() ; i++) {
                double avg = 0;
                for(int j = 0 ; j < splits.length ; j++) {
                    avg += partDistances[j].get(i) / splits.length;
                }
                dists.add(avg);
                System.out.print(avg + " ");
            }
            System.out.println();
            
            return dists;
        } else {
            return super.distancesToBehaviours(behav, all);
        }

    }

    private double[] extract(double[] full, int from, int to) {
        double[] res = new double[to - from];
        System.arraycopy(full, from, res, 0, to - from);
        return res;
    }
}
