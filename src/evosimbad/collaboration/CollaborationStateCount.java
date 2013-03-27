/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.collaboration;

import evosimbad.commons.EpuckBase;
import evosimbad.generic.EpuckStateCount;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class CollaborationStateCount extends EpuckStateCount {

    public CollaborationStateCount(String fit, int updateInterval, int bins, boolean savePatterns) {
        super(fit, updateInterval, bins, savePatterns);
    }

    @Override
    protected double[] readData(Agent ag) {
        CollaborativeEpuck ce = (CollaborativeEpuck) ag;
        double[] obstacleSensor = reduce8to4(ce.readObstacleIR());
        double[] robotSensor = reduce8to4(ce.readRobotIR());
        double[] vec = new double[ce.taskSensor != null ? 20 : 16];
        System.arraycopy(obstacleSensor, 0, vec, 0, 4);
        System.arraycopy(robotSensor, 0, vec, 4, 4);
        vec[8] = ce.readOverTaskSensor();
        vec[9] = ce.readNearCount();
        vec[10] = EpuckBase.normalize(ce.getKinematics().getLeftVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        vec[11] = EpuckBase.normalize(ce.getKinematics().getRightVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        vec[12] = ce.isHardStopped() ? 1 : -1;
        if (ce.taskSensor != null) {
            double[] taskSensor = reduce8to4(ce.readTaskSensor());
            System.arraycopy(taskSensor, 0, vec, 13, 4);
        }
        return vec;
    }
}
