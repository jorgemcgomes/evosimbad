/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.energy.task;

import evosimbad.deprecated.energy.task.TaskRechargeableEpuck;
import evosimbad.core.EvaluationFunction;
import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class TaskFitnessFunction extends InformedCharacterisation {

    private double fitness;

    public TaskFitnessFunction(int updateInterval) {
        super(updateInterval);
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
        int tot = 0;
        int inTask = 0;
        for (Agent a : agents) {
            if (a instanceof TaskRechargeableEpuck) {
                tot++;
                TaskRechargeableEpuck re = (TaskRechargeableEpuck) a;
                if(re.isMakingTask()) {
                    inTask++;
                }
            }
        }
        fitness += (double) inTask / tot / getTotalEvaluations();

    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public double[] getBehaviour() {
        return new double[]{fitness};
    }
}
