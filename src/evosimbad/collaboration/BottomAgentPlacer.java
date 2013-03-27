/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.collaboration;

import evosimbad.core.AgentPlacer;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import org.apache.commons.lang3.tuple.Pair;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author jorge
 */
public class BottomAgentPlacer implements AgentPlacer {

    private double distance;

    public BottomAgentPlacer(double distance) {
        this.distance = distance;
    }

    @Override
    public List<Pair<Point3d, Double>> generateStartPositions(EnvironmentDescription ed, int count) {
        double width = distance * count;
        double x = -ed.worldSize / 2 + distance / 2;
        double startZ = -width / 2;
        List<Pair<Point3d, Double>> res = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            res.add(Pair.of(new Point3d(x, 0, startZ + i * distance), 0d));
        }
        return res;
    }
}
