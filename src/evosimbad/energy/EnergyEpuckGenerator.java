/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.commons.VariableAgentGenerator;
import evosimbad.core.NNAgent;

/**
 *
 * @author Jorge
 */
public class EnergyEpuckGenerator extends VariableAgentGenerator {

    protected float stationSensorRange;
    protected double initialLevel;
    protected double maxLevel;
    protected double dropRate;

    public EnergyEpuckGenerator(int min, int max, float stationSensorRange, double initialLevel, double maxLevel, double dropRate) {
        super(min, max);
        this.stationSensorRange = stationSensorRange;
        this.initialLevel = initialLevel;
        this.maxLevel = maxLevel;
        this.dropRate = dropRate;
    }

    @Override
    protected NNAgent generateAgent() {
        RechargeableEpuck ag = new RechargeableEpuck(initialLevel, maxLevel, dropRate);
        ag.setObstacleIR(0.1f, 8);
        ag.setRobotIR(0.25f, 24);
        ag.setChargingStationSensor(stationSensorRange, 32);
        ag.setEnergySensor();
        ag.setChargingSensor();
        return ag;
    }

    @Override
    public int getInputs() {
        return 8 + 8 + 8 + 1 + 1;
    }

    @Override
    public int getOutputs() {
        return 2;
    }
}
