/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import java.awt.Color;
import java.util.ArrayList;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class PointOfCharge extends Agent {

    protected float chargePower, chargeDistance;
    private Point3d p = new Point3d();
    
    public PointOfCharge(Vector3d pos, float chargePower, float chargeDistance) {
        super(pos);
        this.chargePower = chargePower; 
        this.chargeDistance = chargeDistance;
        
        //this.radius = 0.05f;
        this.height = 0.04f;
        this.setColor(new Color3f(Color.YELLOW));
        this.setCanBeTraversed(true);
    }

    @Override
    protected void performBehavior() {
        this.getCoords(p);
        Point3d selfPos = new Point3d(p);
        selfPos.y = 0;
        ArrayList agentList = super.getSimulator().getAgentList();
        RechargeableEpuck closest = null;
        double minDistance = Double.POSITIVE_INFINITY;
        
        for(Object o : agentList) {
            if(o instanceof RechargeableEpuck) {
                RechargeableEpuck ag = (RechargeableEpuck) o;
                ag.getCoords(p);
                p.y = 0;
                double dist = selfPos.distance(p);
                if(dist <= chargeDistance && dist < minDistance) {
                    closest = ag;
                    minDistance = dist;
                }
            }
        }
        
        if(closest != null) {
            //System.out.println("Charging " + closest.getName());
            closest.charge(chargePower);
        }
    }
}
