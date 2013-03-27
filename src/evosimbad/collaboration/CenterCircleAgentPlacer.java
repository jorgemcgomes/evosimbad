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
public class CenterCircleAgentPlacer implements AgentPlacer {

    private double clearance;

    public CenterCircleAgentPlacer(double clearance) {
        this.clearance = clearance;
    }

    @Override
    public List<Pair<Point3d, Double>> generateStartPositions(EnvironmentDescription ed, int count) {
        double radius = Math.max(clearance / 2, (clearance * count) / (2 * Math.PI));
        double centerX = 0;
        double centerZ = 0;

        List<Pair<Point3d, Double>> points = new ArrayList<>();
        double angleIncrements = (Math.PI * 2) / count;
        for (int i = 0; i < count; i++) {
            double angle = angleIncrements * i;
            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);
            points.add(Pair.of(new Point3d(x,0,z), Math.PI*2 - angle));
        }
        
        return points;
    }
}
