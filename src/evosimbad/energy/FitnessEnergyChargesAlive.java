/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.core.EvaluationFunction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class FitnessEnergyChargesAlive extends EvaluationFunction {
    
    private int alive = 0, total = 0, nCharged = 0;
    private double avgEnergy = 0;
    private double wAverageEnergy, wCharges, wAliveEnd;
    private transient Set<RechargeableEpuck> charged = new HashSet<>(15);

    public FitnessEnergyChargesAlive(int updateInterval, double wAverageEnergy, double wCharges, double wAliveEnd) {
        super(updateInterval);
        this.wAverageEnergy = wAverageEnergy;
        this.wCharges = wCharges;
        this.wAliveEnd = wAliveEnd;
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
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                avgEnergy += re.getEnergy() / re.maxLevel / total / getTotalEvaluations();
                if(re.isCharging()) {
                    charged.add(re);
                }
            }
        }
        nCharged = charged.size();
        
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
        double fit = avgEnergy * wAverageEnergy + ((double) nCharged / total) * wCharges + ((double) alive / total) * wAliveEnd;        
        return fit;
    }
}
