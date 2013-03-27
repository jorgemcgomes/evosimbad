/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class AverageAliveNovelty extends InformedCharacterisation {

    private double alive;
    private double averageEnergy;
    private int averageEnergyCount = 0;
    private int numAgents;

    public AverageAliveNovelty(int updateRate) {
        super(updateRate);
    }

    @Override
    public double[] getBehaviour() {
        return new double[]{averageEnergy / averageEnergyCount, alive};
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();

        // init
        if (numAgents == 0) {
            for (Agent a : agents) {
                if (a instanceof RechargeableEpuck) {
                    numAgents++;
                }
            }
        }

        // average energy of alive robots
        double en = 0;
        int al = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                if(re.getEnergy() > 0.1) {
                    en += re.getEnergy() / re.maxLevel;
                    al++;
                }
            }
        }
        if(al > 0) {
            averageEnergy += en / al;
            averageEnergyCount++;
        }

        // alive at the end
        if (super.getCurrentEvaluation() == super.getTotalEvaluations()) {
            for (Agent a : agents) {
                if (a instanceof RechargeableEpuck) {
                    RechargeableEpuck re = (RechargeableEpuck) a;
                    if (re.getEnergy() > 0.1) {
                        alive += 1.0 / numAgents;
                    }
                }
            }
        }
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
