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
public class EpuckStateCount extends StateCountEvaluation {

    private static final int[] COMPOSITION = new int[]{4, 4, 1, 1, 1, 1};

    public EpuckStateCount(String fit, int updateInterval, int bins, boolean savePatterns) {
        super(fit, updateInterval, bins, savePatterns);
    }

    @Override
    protected double[] readData(Agent ag) {
        Epuck robot = (Epuck) ag;
        double[] obstacleSensor = reduce8to4(robot.readObstacleIR());
        double[] robotSensor = reduce8to4(robot.readRobotIR());
        double[] vec = new double[12];
        System.arraycopy(obstacleSensor, 0, vec, 0, 4);
        System.arraycopy(robotSensor, 0, vec, 4, 4);
        vec[8] = robot.readNearCount();
        vec[9] = EpuckBase.normalize(robot.getKinematics().getLeftVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        vec[10] = EpuckBase.normalize(robot.getKinematics().getRightVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        vec[11] = robot.isHardStopped() ? 1 : -1;
        return vec;
    }

    public static double[] reduce8to4(double[] vals) {
        double[] res = new double[4];
        res[0] = Math.min(Math.min(vals[7], vals[0]), vals[1]); // front
        res[1] = Math.min(Math.min(vals[1], vals[2]), vals[3]); // right
        res[2] = Math.min(Math.min(vals[3], vals[4]), vals[5]); // back
        res[3] = Math.min(Math.min(vals[5], vals[6]), vals[7]); // left
        return res;
    }

    @Override
    protected int[] getComposition() {
        return COMPOSITION;
    }
}
