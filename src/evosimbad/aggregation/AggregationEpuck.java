/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation;

import evosimbad.commons.Epuck;
import java.awt.Color;
import javax.vecmath.Color3f;

/**
 *
 * @author jorge
 */
public class AggregationEpuck extends Epuck {

    private Color3f baseColor = null;
    private Color3f highColor = new Color3f(Color.GREEN);
    private boolean highlight;

    AggregationEpuck(boolean highlight) {
        super(5);
        this.highlight = highlight;
    }

    @Override
    protected void performBehavior(double[] output) {
        super.performBehavior(output);
        // change colors
        if (highlight) {
            if (baseColor == null) {
                baseColor = new Color3f(super.getColor());
            }
            if (robotIR != null && robotIR.oneHasHit()) {
                this.setColor(highColor);
            } else {
                this.setColor(baseColor);
            }
        }
    }
}
