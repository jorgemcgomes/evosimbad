/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import evosimbad.core.NNAgent;
import simbad.sim.DifferentialKinematic;

/**
 *
 * @author Jorge
 */
public abstract class EpuckBase extends NNAgent {

    public static double WHEEL_DISTANCE = 0.053f;
    public static double MAX_WHEEL_VELOCITY = (float) (Math.PI * 0.041); // wheel diameter = 41mm, 1 revolution per second
    protected DifferentialKinematic kinematics;
    protected int nInputs = 0;
    protected int updatesPerSecond;
    protected boolean stopActuatorEnabled = false;
    protected boolean isHardStopped = false;

    public EpuckBase(int updatesPerSecond) {
        this.kinematics = new DifferentialKinematic(WHEEL_DISTANCE);
        super.setKinematicModel(kinematics);
        this.updatesPerSecond = updatesPerSecond;
    }

    protected void setMotorsSpeed(double left, double right) {
        kinematics.setLeftVelocity(denormalize(left, -MAX_WHEEL_VELOCITY, MAX_WHEEL_VELOCITY));
        kinematics.setRightVelocity(denormalize(right, -MAX_WHEEL_VELOCITY, MAX_WHEEL_VELOCITY));
    }

    @Override
    protected void performBehavior(double[] output) {
        if(stopActuatorEnabled && output[2] > 0.5) {
            isHardStopped = true;
            kinematics.setLeftVelocity(0);
            kinematics.setRightVelocity(0);
        } else {
            isHardStopped = false;
            setMotorsSpeed(output[0], output[1]);
        }
    }
    
    public DifferentialKinematic getKinematics() {
        return kinematics;
    }
        
    public void toggleStopActuator(boolean b) {
        this.stopActuatorEnabled = b;
    }

    public boolean isHardStopped() {
        return isHardStopped;
    }
    
    public boolean hasStopActuatorEnabled() {
        return stopActuatorEnabled;
    }
    
}
