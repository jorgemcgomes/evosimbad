/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.collaboration;

import evosimbad.commons.SquareGenerator;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author jorge
 */
public class TaskEnvironmentGenerator extends SquareGenerator {

    private int nTasks;
    private float workPerTask;
    private float radius;
    private float clearance;
    private float safeArea;

    public TaskEnvironmentGenerator(float worldSize, float safeArea, int nTasks, float radius, float clearance, float workPerTask) {
        super(worldSize);
        this.nTasks = nTasks;
        this.workPerTask = workPerTask;
        this.radius = radius;
        this.clearance = clearance;
        this.safeArea = safeArea;
    }

    @Override
    public EnvironmentDescription generateEnvironment() {
        EnvironmentDescription env = super.generateEnvironment();

        Point2d center = new Point2d(0,0);
        List<Point2d> placed = new ArrayList<>(nTasks);
        float bound = env.worldSize / 2 - radius;
        while (placed.size() < nTasks) {
            float x = (float) ((Math.random() * 2 - 1) * bound);
            float z = (float) ((Math.random() * 2 - 1) * bound);
            Point2d candidate = new Point2d(x, z);
            if(candidate.distance(center) < safeArea) {
                continue;
            }
            boolean isValid = true;
            for (Point2d p : placed) {
                if (p.distance(candidate) < clearance) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                placed.add(candidate);
            }
        }

        for (Point2d p : placed) {
            TaskSpot ts = new TaskSpot(new Vector3d(p.x, 0f , p.y), radius, workPerTask);
            env.add(ts);
        }
        return env;
    }
}
