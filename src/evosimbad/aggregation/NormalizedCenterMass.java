/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation;

/**
 *
 * @author jorge
 */
public class NormalizedCenterMass extends AggregationCenterMass {

    boolean normalized = false;

    public NormalizedCenterMass(int updateRate) {
        super(updateRate);
    }

    @Override
    public double[] getBehaviour() {
        if (!normalized) {
            double min = 0.15, max = 0.80;
            for (int i = 0; i < super.getBehaviour().length; i++) {
                super.getBehaviour()[i] = Math.max(Math.min((super.getBehaviour()[i] - min) / (max - min), 1), 0);
            }
            normalized = true;
        }
        return super.getBehaviour();
    }
}
