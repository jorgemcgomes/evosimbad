/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.core.EvaluationFunction;
import java.util.ArrayList;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class FitnessMinAverage extends EvaluationFunction {
    
    private int alive = 0, total = 0;
    private double avgEnergy = 0;
    private double minEnergy = 0;
    private double wAverage, wMinimum, wAlive;

    public FitnessMinAverage(int updateInterval, double wAverage, double wMinimum, double wAlive) {
        super(updateInterval);
        this.wAverage = wAverage;
        this.wMinimum = wMinimum;
        this.wAlive = wAlive;
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
        // init
        if (total == 0) {
            for (Agent a : agents) {
                if (a instanceof RechargeableEpuck) {
                    total++;
                }
            }
        }
        // average & min
        double min = Double.POSITIVE_INFINITY;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                avgEnergy += re.getEnergy() / re.maxLevel / total / getTotalEvaluations();
                min = Math.min(min, re.getEnergy() / re.maxLevel);
            }
        }
        minEnergy += min / getTotalEvaluations();
        
        // alive at the end
        if (super.getCurrentEvaluation() == super.getTotalEvaluations()) {
            for (Agent a : agents) {
                if (a instanceof RechargeableEpuck) {
                    RechargeableEpuck re = (RechargeableEpuck) a;
                    if (re.getEnergy() > 0.1) {
                        alive++;
                    }
                }
            }
        }
    }

    @Override
    public double getFitness() {
        double fit = avgEnergy * wAverage + minEnergy * wMinimum + ((double) alive / total) * wAlive;        
        return fit;
    }
}
