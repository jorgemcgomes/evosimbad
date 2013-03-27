/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.escape;

import evosimbad.commons.RandomAgentPlacer;
import evosimbad.escape.EscapeEnvironment.EscapeEnv;
import java.util.LinkedList;
import javax.vecmath.Point3d;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author jorge
 */
public class EscapeAgentPlacer extends RandomAgentPlacer {

    private float minDoorDistance;
    
    public EscapeAgentPlacer(float safeMargin, float separation, float minDoorDistance) {
        super(safeMargin, separation);
        this.minDoorDistance = minDoorDistance;
    }

    @Override
    protected boolean isValid(LinkedList<Point3d> placed, Point3d candidate, EnvironmentDescription ed) {
        EscapeEnv env = (EscapeEnv) ed;
        if(env.getDoorLocation().distance(candidate) < minDoorDistance) {
            return false;
        } else {
            return super.isValid(placed, candidate, ed);
        }
    }
}
