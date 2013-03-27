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
 * @author Jorge
 */
public class NoveltyDifferentCharging extends InformedCharacterisation {

    private transient HashSet<RechargeableEpuck> charged = new HashSet<>(10);
    private int numCharged;
    private int total;

    public NoveltyDifferentCharging() {
        super(1);
    }

    @Override
    public double[] getBehaviour() {
        return new double[]{(double) numCharged / total};
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

        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                if (re.isCharging()) {
                    charged.add(re);
                }
            }
        }
        numCharged = charged.size();
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
