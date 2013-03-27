/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.commons.Epuck;
import evosimbad.commons.EpuckBase;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class EpuckCentroid extends GenericCentroidCharacterisation {

    public EpuckCentroid(String fit, int updateRate, int timeSteps) {
        super(fit, updateRate, timeSteps);
    }

    @Override
    public double[] readData(Agent ag) {
        Epuck robot = (Epuck) ag;
        double[] obstacleSensor = EpuckStateCount.reduce8to4(robot.readObstacleIR());
        double[] robotSensor = EpuckStateCount.reduce8to4(robot.readRobotIR());
        double[] vec = new double[12];
        System.arraycopy(obstacleSensor, 0, vec, 0, 4);
        System.arraycopy(robotSensor, 0, vec, 4, 4);
        vec[8] = robot.readNearCount();
        vec[9] = EpuckBase.normalize(robot.getKinematics().getLeftVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        vec[10] = EpuckBase.normalize(robot.getKinematics().getRightVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        vec[11] = robot.isHardStopped() ? 1 : -1;
        return vec;
    }
}
