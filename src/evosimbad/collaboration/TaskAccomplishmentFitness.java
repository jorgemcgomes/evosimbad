/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.collaboration;

import evosimbad.core.EvaluationFunction;
import java.util.ArrayList;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class TaskAccomplishmentFitness extends EvaluationFunction {

    private int stepsWithTasks = 0;
    private int workingRobots = 0;
    private double totalLoad = 0;
    private double remaining = Double.POSITIVE_INFINITY;

    public TaskAccomplishmentFitness(int updateInterval) {
        super(updateInterval);
    }

    @Override
    public void evaluate() {
        if (remaining > 0.001) {
            ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
            remaining = 0;
            for (Agent ag : agents) {
                if (ag instanceof TaskSpot) {
                    TaskSpot ts = (TaskSpot) ag;
                    remaining += ts.remainingWork;
                }
            }
            if (getCurrentEvaluation() == 1) {
                totalLoad = remaining;
                for (Agent ag : agents) {
                    if (ag instanceof CollaborativeEpuck) {
                        workingRobots++;
                    }
                }
            }
            if (remaining > 0.001) {
                stepsWithTasks += getUpdateInterval();
            }
        }
    }

    @Override
    public double getFitness() {
        double workRate = (totalLoad - remaining) / (double) stepsWithTasks;
        double maxWorkRate = Math.pow(workingRobots, 2);
        return workRate / maxWorkRate;
    }
}
