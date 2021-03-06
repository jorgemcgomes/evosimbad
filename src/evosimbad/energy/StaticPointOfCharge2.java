/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import java.util.ArrayList;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Jorge
 */
public class StaticPointOfCharge2 extends PointOfCharge {

    private int delay;
    private Point3d p = new Point3d();
    private RechargeableEpuck charging;
    private int permanence = 0;

    public StaticPointOfCharge2(Vector3d pos, float chargePower, float chargeDistance, int delay) {
        super(pos, chargePower, chargeDistance);
        this.delay = delay;
    }

    @Override
    protected void performBehavior() {
        this.getCoords(p);
        p.y = 0;
        Point3d selfPos = new Point3d(p);

        // verificar se continua a carregar o que ja estava
        if (charging != null && !charging.isHardStopped()) {
            charging.isCharging = false;
            charging = null;
            permanence = 0;
        }

        // determinar qual deve carregar, caso nao esteja a carregar nenhum
        if (charging == null) {
            ArrayList agentList = super.getSimulator().getAgentList();
            RechargeableEpuck closest = null;
            double minDistance = Double.POSITIVE_INFINITY;
            for (Object o : agentList) {
                if (o instanceof RechargeableEpuck) {
                    RechargeableEpuck ag = (RechargeableEpuck) o;
                    ag.getCoords(p);
                    p.y = 0;
                    double dist = selfPos.distance(p);
                    if (dist <= chargeDistance && dist < minDistance) {
                        closest = ag;
                        minDistance = dist;
                    }
                }
            }
            if (closest != null) {
                charging = closest;
                charging.isCharging = true;
            }
        }

        // carregar
        if (charging != null) {
            if (permanence > delay) {
                charging.charge(chargePower);
            } else {
                permanence++;
            }
        }
    }
}
