/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.energy.RechargeableEpuck;
import java.util.Arrays;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class REpuckStateCount2 extends REpuckStateCount {

    public REpuckStateCount2(String fit, int updateInterval, int bins, boolean savePatterns) {
        super(fit, updateInterval, bins, savePatterns);
    }

    @Override
    protected double[] readData(Agent ag) {
        RechargeableEpuck re = (RechargeableEpuck) ag;
        if(re.getEnergy() > 0.01) {
            return super.readData(ag);
        } else {
            double[] dummy = new double[16];
            Arrays.fill(dummy, bins*2);
            return dummy;
        }
    }

}
