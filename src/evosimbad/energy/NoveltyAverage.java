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
public class NoveltyAverage extends InformedCharacterisation {

    private double[] behav;

    public NoveltyAverage(int updateRate) {
        super(updateRate);
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
        // init
        if (behav == null) {
            behav = new double[getTotalEvaluations()];
        }
        // novelty measure
        double average = 0;
        int alive = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                if (re.getEnergy() > 0.1) {
                    average += re.getEnergy() / re.maxLevel;
                    alive++;
                }
            }
        }
        behav[getCurrentEvaluation() - 1] = alive == 0 ? 0 : average / alive;
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
