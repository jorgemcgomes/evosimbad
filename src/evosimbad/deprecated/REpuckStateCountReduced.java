/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.commons.EpuckBase;
import evosimbad.energy.RechargeableEpuck;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class REpuckStateCountReduced extends REpuckStateCount {

    public REpuckStateCountReduced(String fit, int updateInterval, int bins, boolean savePatterns) {
        super(fit, updateInterval, bins, savePatterns);
    }
    
        @Override
    protected double[] readData(Agent ag) {
        RechargeableEpuck robot = (RechargeableEpuck) ag;
        double obstacleSensor = vectorMin(robot.readObstacleIR());
        double robotSensor = vectorMin(robot.readRobotIR());
        double stationSensor = vectorMin(robot.readChargingStationSensor());
        double[] vec = new double[7];
        vec[0] = obstacleSensor;
        vec[1] = robotSensor;
        vec[2] = stationSensor;
        vec[3] = robot.readChargingSensor();
        vec[4] = robot.readEnergySensor();
        vec[5] = EpuckBase.normalize(robot.getKinematics().getLeftVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        vec[6] = EpuckBase.normalize(robot.getKinematics().getRightVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        return vec;
    }
        
    protected double vectorMin(double[] vec) {
        double min = Double.POSITIVE_INFINITY;
        for(double d : vec) {
            min = Math.min(min, d);
        }
        return min;
    }    
    
}
