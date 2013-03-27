/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.commons.VariableAgentGenerator;
import evosimbad.core.NNAgent;

/**
 *
 * @author jorge
 */
public class VariableEnergyEpuckGenerator extends VariableAgentGenerator {

    protected float stationSensorRange;
    protected double initialLevel;
    protected double maxLevel;
    protected double minDropRate;
    protected double maxDropRate;

    public VariableEnergyEpuckGenerator(int min, int max, float stationSensorRange, double initialLevel, double maxLevel, double minDropRate, double maxDropRate) {
        super(min, max);
        this.stationSensorRange = stationSensorRange;
        this.initialLevel = initialLevel;
        this.maxLevel = maxLevel;
        this.minDropRate = minDropRate;
        this.maxDropRate = maxDropRate;
    }

    @Override
    protected NNAgent generateAgent() {
        RechargeableEpuck ag = new VariableRechargeableEpuck(initialLevel, maxLevel, minDropRate, maxDropRate);
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
