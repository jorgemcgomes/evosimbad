/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import java.util.HashSet;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class NoveltyCharging extends InformedCharacterisation {

    private HashSet<RechargeableEpuck> charging = new HashSet<>(20);
    private int total = 0;
    private double[] behav;

    public NoveltyCharging(int updateRate) {
        super(updateRate);
    }

    @Override
    public void step() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                if (re.isCharging()) {
                    charging.add(re);
                }
            }
        }
        super.step();
    }

    @Override
    public double[] getBehaviour() {
        return behav;
    }

    @Override
    public void evaluate() {
        // init
        if (behav == null) {
            ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
            for (Agent a : agents) {
                if (a instanceof RechargeableEpuck) {
                    total++;
                }
            }
            behav = new double[getTotalEvaluations()];
        }

        behav[getCurrentEvaluation() - 1] = (double) charging.size() / total;
        charging.clear();
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
