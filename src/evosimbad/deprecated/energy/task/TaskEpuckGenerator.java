/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.energy.task;

import evosimbad.deprecated.energy.task.TaskRechargeableEpuck;
import evosimbad.core.NNAgent;
import evosimbad.energy.VariableEnergyEpuckGenerator;

/**
 *
 * @author jorge
 */
public class TaskEpuckGenerator extends VariableEnergyEpuckGenerator {

    private double cornerSize, taskDropRate;
    
    public TaskEpuckGenerator(int min, int max, float stationSensorRange, double initialLevel, double maxLevel, double minDropRate, double maxDropRate, double cornerSize, double taskDropRate) {
        super(min, max, stationSensorRange, initialLevel, maxLevel, minDropRate, maxDropRate);
        this.cornerSize = cornerSize;
        this.taskDropRate = taskDropRate;
    }

    @Override
    protected NNAgent generateAgent() {
        TaskRechargeableEpuck ag = new TaskRechargeableEpuck(initialLevel, maxLevel, minDropRate, maxDropRate, taskDropRate);
        ag.setObstacleIR(0.1f, 8);
        ag.setRobotIR(0.25f, 24);
        ag.setChargingStationSensor(stationSensorRange, 32);
        ag.setEnergySensor();
        ag.setChargingSensor();
        ag.setTaskSensor(cornerSize);
        return ag;
    }

    @Override
    public int getInputs() {
        return 8 + 8 + 8 + 1 + 1 + 1;
    }
}
