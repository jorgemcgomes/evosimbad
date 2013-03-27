/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.energy.task;

import evosimbad.energy.StaticSingleChargeArena;
import simbad.sim.Box;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author jorge
 */
public class TaskArena extends StaticSingleChargeArena {

    private double cornerSize;
    
    public TaskArena(float worldSize, float chargeRate, float chargeDistance, int delay, double cornerSize) {
        super(worldSize, chargeRate, chargeDistance, delay);
        this.cornerSize = cornerSize;
    }

    @Override
    public EnvironmentDescription generateEnvironment() {
        EnvironmentDescription ed = super.generateEnvironment();
        return null;
    }
}
