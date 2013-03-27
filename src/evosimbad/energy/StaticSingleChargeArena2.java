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
public class StaticSingleChargeArena2 extends SquareGenerator {

    private float chargeRate;
    private float chargeDistance;
    private int delay;
    
    public StaticSingleChargeArena2(float worldSize, float chargeRate, float chargeDistance, int delay) {
        super(worldSize);
        this.chargeRate = chargeRate;
        this.chargeDistance = chargeDistance;
        this.delay = delay;
    }

    @Override
    public EnvironmentDescription generateEnvironment() {
        EnvironmentDescription ed = super.generateEnvironment();
        
        Vector3d pos = new Vector3d(0,0,0);
        StaticPointOfCharge2 station = new StaticPointOfCharge2(pos, chargeRate, chargeDistance, delay);
        ed.add(station);
        
        return ed;
    }
       
}
