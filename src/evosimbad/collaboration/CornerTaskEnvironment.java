/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.collaboration;

import evosimbad.commons.SquareGenerator;
import javax.vecmath.Vector3d;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author jorge
 */
public class CornerTaskEnvironment extends SquareGenerator {

    private float workPerTask;
    private float radius;

    public CornerTaskEnvironment(float worldSize, float radius, float workPerTask) {
        super(worldSize);
        this.workPerTask = workPerTask;
        this.radius = radius;
    }

    @Override
    public EnvironmentDescription generateEnvironment() {
        EnvironmentDescription env = super.generateEnvironment();
        env.add(new TaskSpot(new Vector3d(-env.worldSize / 2, 0f, -env.worldSize/2), radius, workPerTask));
        env.add(new TaskSpot(new Vector3d(env.worldSize / 2, 0f, -env.worldSize/2), radius, workPerTask));
        env.add(new TaskSpot(new Vector3d(-env.worldSize / 2, 0f, env.worldSize/2), radius, workPerTask));
        env.add(new TaskSpot(new Vector3d(env.worldSize / 2, 0f, env.worldSize/2), radius, workPerTask));

        return env;
    }
}
