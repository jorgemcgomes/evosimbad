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
public class FitnessBastos extends EvaluationFunction {

    private int alive = 0, total = 0;
    private double minEnergy = Double.POSITIVE_INFINITY;
    private double wEnergy, wAlive; // 0.9 - 0.1 bastos

    public FitnessBastos(double wEnergy, double wAlive) {
        super(1);
        this.wEnergy = wEnergy;
        this.wAlive = wAlive;
    }

    @Override
    public void evaluate() {
        if (super.getCurrentEvaluation() == super.getTotalEvaluations()) {
            ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
            for (Agent a : agents) {
                if (a instanceof RechargeableEpuck) {
                    total++;
                    RechargeableEpuck re = (RechargeableEpuck) a;
                    if (re.getEnergy() > 0.1) {
                        alive++;
                    }
                    minEnergy = Math.min(minEnergy, re.getEnergy() / re.maxLevel);
                }
            }
        }
    }

    @Override
    public double getFitness() {
        return minEnergy * wEnergy + ((double) alive / total) * wAlive;
    }
}
