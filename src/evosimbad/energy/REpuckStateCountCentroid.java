/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.commons.EpuckBase;
import evosimbad.generic.EpuckStateCount;
import evosimbad.generic.GenericCentroidCharacterisation;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class REpuckStateCountCentroid extends GenericCentroidCharacterisation {

    public REpuckStateCountCentroid(String fit, int updateRate, int timeSteps) {
        super(fit, updateRate, timeSteps);
    }

    @Override
    public double[] readData(Agent ag) {
        RechargeableEpuck re = (RechargeableEpuck) ag;
        if (re.getEnergy() > 0.01) {
            double[] obstacleSensor = EpuckStateCount.reduce8to4(re.readObstacleIR());
            double[] robotSensor = EpuckStateCount.reduce8to4(re.readRobotIR());
            double[] stationSensor = EpuckStateCount.reduce8to4(re.readChargingStationSensor());
            double[] vec = new double[17];
            System.arraycopy(obstacleSensor, 0, vec, 0, 4);
            System.arraycopy(robotSensor, 0, vec, 4, 4);
            System.arraycopy(stationSensor, 0, vec, 8, 4);
            vec[12] = re.readChargingSensor();
            vec[13] = re.readEnergySensor();
            vec[14] = EpuckBase.normalize(re.getKinematics().getLeftVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
            vec[15] = EpuckBase.normalize(re.getKinematics().getRightVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
            vec[16] = re.isHardStopped() ? 1 : -1;
            return vec;
        } else {
            return new double[]{1, 1, 1, 1,
                        1, 1, 1, 1,
                        1, 1, 1, 1,
                        -1, -1, 0, 0, -1};
        }
    }
}
