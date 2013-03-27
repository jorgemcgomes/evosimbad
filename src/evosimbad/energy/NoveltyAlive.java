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
 * @author Jorge
 */
public class NoveltyAlive extends InformedCharacterisation {

    private int total = 0;
    private double[] behav;

    public NoveltyAlive(int updateRate) {
        super(updateRate);
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

        // novelty measure
        int count = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                if (re.getEnergy() > 0.1) {
                    count++;
                }
            }
        }
        behav[getCurrentEvaluation() - 1] = (double) count / total;
    }

    @Override
    public double getFitness() {
        return 0;
    }

    @Override
    public double[] getBehaviour() {
        return behav;
    }
}
