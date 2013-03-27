/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.energy.task;

import evosimbad.commons.Epuck;
import evosimbad.energy.VariableRechargeableEpuck;
import javax.vecmath.Vector3d;
import org.apache.commons.lang3.ArrayUtils;
import simbad.sim.CornerSensor;

/**
 *
 * @author jorge
 */
public class TaskRechargeableEpuck extends VariableRechargeableEpuck {

    private CornerSensor cornerSensor;
    private double taskDropRate;

    public TaskRechargeableEpuck(double initialLevel, double maxLevel, double minDropRate, double maxDropRate, double taskDropRate) {
        super(initialLevel, maxLevel, minDropRate, maxDropRate);
        this.taskDropRate = taskDropRate;
    }

    public void setTaskSensor(double cornerSize) {
        Vector3d pos = new Vector3d(0, 0, 0);
        cornerSensor = new CornerSensor(cornerSize);
        cornerSensor.setUpdatePerSecond(updatesPerSecond);
        super.addSensorDevice(cornerSensor, pos, 0);
    }

    public double readTaskSensor() {
        return normalize(cornerSensor.read(), 0, 1);
    }

    @Override
    public double[] readSensors() {
        double[] readSensors = super.readSensors();
        return ArrayUtils.add(readSensors, readTaskSensor());
    }

    public boolean isMakingTask() {
        return cornerSensor.read() == 1 && energyLevel > 0.1;
    }

    @Override
    protected void performBehavior(double[] output) {
        if(isMakingTask()) {
            energyLevel = Math.max(0, energyLevel - taskDropRate);
        } else if (energyLevel > 0.1) {
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
