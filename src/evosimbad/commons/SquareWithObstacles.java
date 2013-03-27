/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import evosimbad.core.EnvironmentGenerator;
import java.util.ArrayList;
import java.util.List;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import simbad.sim.Box;
import simbad.sim.EnvironmentDescription;
import simbad.sim.StaticObject;
import simbad.sim.Wall;

/**
 *
 * @author Jorge
 */
public class SquareWithObstacles implements EnvironmentGenerator {

    private double minWallSeparation, minBoxSeparation, minBoxSide, maxBoxSide;
    private float worldSize;
    private int minNumberBoxes, maxNumberBoxes;
    private static final float HEIGHT = 0.2f;

    public SquareWithObstacles(float worldSize, double minBoxSeparation, double maxBoxSide, int maxNumberBoxes) {
        this(worldSize, -1, minBoxSeparation, 0.05f, maxBoxSide, 1, maxNumberBoxes);
    }

    public SquareWithObstacles(float worldSize, double minWallSeparation, double minBoxSeparation, double minBoxSide, double maxBoxSide, int minNumberBoxes, int maxNumberBoxes) {
        this.worldSize = worldSize;
        this.minWallSeparation = minWallSeparation;
        this.minBoxSeparation = minBoxSeparation;
        this.minBoxSide = minBoxSide;
        this.maxBoxSide = maxBoxSide;
        this.minNumberBoxes = minNumberBoxes;
        this.maxNumberBoxes = maxNumberBoxes;
    }

    @Override
    public EnvironmentDescription generateEnvironment() {
        EnvironmentDescription env = new EnvironmentDescription();
        env.worldSize = worldSize;
        env.setUsePhysics(true);
        float thickness = 0.2f;

        ArrayList<BoundingBox> wallBounds = new ArrayList<>(4);
        wallBounds.add(new BoundingBox(new Point3d(-worldSize / 2, 0, -worldSize / 2),
                new Point3d(-worldSize / 2 + thickness, 0, worldSize / 2))); // left
        wallBounds.add(new BoundingBox(new Point3d(-worldSize / 2, 0, worldSize / 2 - thickness),
                new Point3d(worldSize / 2, 0, worldSize / 2))); // top
        wallBounds.add(new BoundingBox(new Point3d(worldSize / 2 - thickness, 0, -worldSize / 2),
                new Point3d(worldSize / 2, 0, worldSize / 2))); // right
        wallBounds.add(new BoundingBox(new Point3d(-worldSize / 2, 0, -worldSize / 2),
                new Point3d(worldSize / 2, 0, -worldSize / 2 + thickness))); // bottom        

        Wall w1 = new Wall(new Vector3d(worldSize / 2, 0, 0), worldSize, thickness, env);
        w1.rotate90(1);
        env.add(w1);
        Wall w2 = new Wall(new Vector3d(-worldSize / 2, 0, 0), worldSize, thickness, env);
        w2.rotate90(1);
        env.add(w2);
        Wall w3 = new Wall(new Vector3d(0, 0, worldSize / 2), worldSize, thickness, env);
        env.add(w3);
        Wall w4 = new Wall(new Vector3d(0, 0, -worldSize / 2), worldSize, thickness, env);
        env.add(w4);

        int numBoxes = minNumberBoxes + (int) Math.round(Math.random() * (maxNumberBoxes - minNumberBoxes));
        ArrayList<BoundingBox> present = new ArrayList<>(numBoxes);
        while (present.size() < numBoxes) {
            double xSide = minBoxSide + Math.random() * (maxBoxSide - minBoxSide);
            double zSide = minBoxSide + Math.random() * (maxBoxSide - minBoxSide);
            Vector3f dimension = new Vector3f((float) xSide, HEIGHT, (float) zSide);
            double xPos = Math.random() * env.worldSize - env.worldSize / 2;
            double zPos = Math.random() * env.worldSize - env.worldSize / 2;
            Vector3d pos = new Vector3d(xPos, 0, zPos);
            BoundingBox bound = new BoundingBox(
                    new Point3d(xPos - xSide / 2, 0, zPos - zSide / 2),
                    new Point3d(xPos + xSide / 2, 0, zPos + zSide / 2));
            
            if (isValid(bound, present, minBoxSeparation) && isValid(bound, wallBounds, minWallSeparation)) {
                present.add(bound);
                Obstacle o = new Obstacle(pos, dimension, env, bound);
                env.add(o);
            }
        }
        return env;
    }

    private boolean isValid(BoundingBox candidate, List<BoundingBox> present, double minSeparation) {
        for (BoundingBox b : present) {
            if (distance(candidate, b) < minSeparation) {
                return false;
            }
        }
        return true;
    }

    // http://www.gamedev.net/topic/568111-any-quick-way-to-calculate-min-distance-between-2-bounding-boxes/
    private double distance(BoundingBox b1, BoundingBox b2) {
        Point3d point = new Point3d();
        double[] b1Max = new double[3], b1Min = new double[3], b2Max = new double[3], b2Min = new double[3];
        b1.getUpper(point);
        point.get(b1Max);
        b1.getLower(point);
        point.get(b1Min);
        b2.getUpper(point);
        point.get(b2Max);
        b2.getLower(point);
        point.get(b2Min);

        double sqrDist = 0;
        for (int i = 0; i < 3; i++) {
            if (b1Max[i] < b2Min[i]) {
                sqrDist += Math.pow(b1Max[i] - b2Min[i], 2);
            } else if (b1Min[i] > b2Max[i]) {
                sqrDist += Math.pow(b1Min[i] - b2Max[i], 2);
            }
        }
        return Math.sqrt(sqrDist);
    }

    public static class Obstacle extends Box {

        private Bounds realBounds;

        public Obstacle(Vector3d pos, Vector3f extent, EnvironmentDescription wd, Bounds realBounds) {
            super(pos, extent, wd);
            this.realBounds = realBounds;
        }

        public Bounds getRealBounds() {
            return realBounds;
        }
    }
}
