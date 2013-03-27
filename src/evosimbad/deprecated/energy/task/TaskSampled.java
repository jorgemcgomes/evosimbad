/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.energy.task;

import evosimbad.energy.RechargeableEpuck;
import evosimbad.deprecated.energy.task.TaskRechargeableEpuck;
import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class TaskSampled extends InformedCharacterisation {
    
    private int total = 0;
    private double[] behav;

    public TaskSampled(int updateRate) {
        super(updateRate);
    }

    @Override
    public double[] getBehaviour() {
        return behav;
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();

        // init
        if (behav == null) {
            for (Agent a : agents) {
                if (a instanceof RechargeableEpuck) {
                    total++;
                }
            }
            behav = new double[getTotalEvaluations()];
        }

        // measure movement
        double tasking = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                TaskRechargeableEpuck re = (TaskRechargeableEpuck) a;
                if(re.isMakingTask()) {
                    tasking++;
                }
            }
        }
        
        behav[getCurrentEvaluation() - 1] = (double) tasking / total;
    }

    @Override
    public double getFitness() {
        return 0;
    }
    
}
