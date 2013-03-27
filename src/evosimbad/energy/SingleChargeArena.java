/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.commons.SquareGenerator;
import javax.vecmath.Vector3d;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author Jorge
 */
public class SingleChargeArena extends SquareGenerator {

    private float chargeRate;
    private float chargeDistance;
    
    public SingleChargeArena(float worldSize, float chargeRate, float chargeDistance) {
        super(worldSize);
        this.chargeRate = chargeRate;
        this.chargeDistance = chargeDistance;
    }

    @Override
    public EnvironmentDescription generateEnvironment() {
        EnvironmentDescription ed = super.generateEnvironment();
        
        Vector3d pos = new Vector3d(0,0,0);
        PointOfCharge station = new PointOfCharge(pos, chargeRate, chargeDistance);
        ed.add(station);
        
        return ed;
    }
       
}
