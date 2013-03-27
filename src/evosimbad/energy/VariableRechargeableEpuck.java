/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.commons.Epuck;

/**
 *
 * @author jorge
 */
public class VariableRechargeableEpuck extends RechargeableEpuck {

    protected double minDropRate, motorDropRate;
    
    public VariableRechargeableEpuck(double initialLevel, double maxLevel, double minDropRate, double maxDropRate) {
        super(initialLevel, maxLevel, maxDropRate);
        this.minDropRate = minDropRate;
        this.motorDropRate = (maxDropRate - minDropRate) / 2;
    }

    @Override
    protected void performBehavior(double[] output) {
        if (energyLevel > 0.1) {
            behavior(output);
            double leftV = Math.abs(kinematics.getLeftVelocity()) / Epuck.MAX_WHEEL_VELOCITY;
            double rightV = Math.abs(kinematics.getRightVelocity()) / Epuck.MAX_WHEEL_VELOCITY;
            double drop = minDropRate + motorDropRate * leftV + motorDropRate * rightV;
            energyLevel = Math.max(0, energyLevel - drop);
        } else {
            kinematics.setLeftVelocity(0);
            kinematics.setRightVelocity(0);
        }
    }
}
