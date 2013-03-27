/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.dispersion;

import evosimbad.core.AgentPlacer;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import org.apache.commons.lang3.tuple.Pair;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author jorge
 */
public class RandomCenterAgentPlacer implements AgentPlacer {

    protected double clearDistance;

    public RandomCenterAgentPlacer(double clearDistance) {
        this.clearDistance = clearDistance;
    }

    @Override
    public List<Pair<Point3d, Double>> generateStartPositions(EnvironmentDescription ed, int count) {
        Point2d center = generateCenterPoint(ed, count);
        List<Pair<Point3d, Double>> pos = generateAround(ed, count, center);
        return pos;
    }
    
    protected Point2d generateCenterPoint(EnvironmentDescription ed, int count) {
        return new Point2d(0,0);
    }

    protected List<Pair<Point3d, Double>> generateAround(EnvironmentDescription ed, int count, Point2d center) {
        List<Point3d> placed = new ArrayList<>(count);
        double r = 0;
        double bound = ed.worldSize / 2 - clearDistance;
        while (placed.size() < count) {
            // the greater the radius, the more tries are allowed
            int tries = Math.max(1, (int) Math.round(Math.PI * Math.pow(r, 2) / clearDistance * 3));
            for (int i = 0; i < tries && placed.size() < count; i++) {
                // generate random point in radius r
                double randomAngle = Math.random() * 2 * Math.PI;
                double x = center.x + r * Math.cos(randomAngle);
                double z = center.y + r * Math.sin(randomAngle);
                
                // check if point is inside environment boundaries
                if(x > bound || x < -bound || z > bound || z < -bound) {
                    continue;
                }
                
                Point3d candidate = new Point3d(x, 0, z);

                // check if point is viable in relation to other points
                boolean good = true;
                for (Point3d p : placed) {
                    if (candidate.distance(p) < clearDistance) {
                        good = false;
                        break;
                    }
                }

                // add if ok
                if (good) {
                    placed.add(candidate);
                }
            }
            r += clearDistance / 3;
        }

        // generate random orientations and assemble in adequate data structure
        List<Pair<Point3d, Double>> res = new ArrayList<>(count);
        for(Point3d p : placed) {
            double rot = Math.random() * Math.PI * 2;
            res.add(Pair.of(p, rot));
        }
        return res;
    }
}
