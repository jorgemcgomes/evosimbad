/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.collaboration;

import java.awt.Color;
import java.util.ArrayList;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class TaskSpot extends Agent {

    protected double totalWork;
    protected double remainingWork;

    public TaskSpot(Vector3d pos, float radius, double workAmount) {
        super(pos);
        this.radius = radius;
        this.height = super.height - 0.001f;
        this.setColor(new Color3f(Color.YELLOW));
        this.setCanBeTraversed(true);
        this.totalWork = workAmount;
        this.remainingWork = totalWork;
    }

    @Override
    protected void performBehavior() {
        Point3d selfPos = new Point3d();
        this.getCoords(selfPos);
        Point3d otherPos = new Point3d();
        ArrayList agentList = super.getSimulator().getAgentList();
        int performingCount = 0;
        for (Object o : agentList) {
            if (o instanceof CollaborativeEpuck) {
                CollaborativeEpuck ag = (CollaborativeEpuck) o;
                ag.getCoords(otherPos);
                double dist = selfPos.distance(otherPos);
                if (dist <= this.radius - ag.getRadius() && ag.isHardStopped()) {
                    performingCount++;
                }

            }
        }

        double work = Math.pow(performingCount, 2);
        remainingWork = Math.max(0, remainingWork - work);
        if (remainingWork < 0.0001) {
            this.setColor(new Color3f(Color.WHITE));
            this.moveToPosition(1000, 1000);
        }
    }
}
