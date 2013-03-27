/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.dispersion;

import javax.vecmath.Point2d;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author jorge
 */
public class RandomAggregatedAgentPlacer extends RandomCenterAgentPlacer {

    public RandomAggregatedAgentPlacer(double clearDistance) {
        super(clearDistance);
    }

    @Override
    protected Point2d generateCenterPoint(EnvironmentDescription ed, int count) {
        double bound = ed.worldSize / 2 - clearDistance;
        double x = (Math.random() * 2 - 1) * bound;
        double y = (Math.random() * 2 - 1) * bound;
        return new Point2d(x, y);

    }
}
