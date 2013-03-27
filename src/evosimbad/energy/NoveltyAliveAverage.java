/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.commons.Epuck;
import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class NoveltyAliveAverage extends InformedCharacterisation {

    private double averageAlive;
    private int averageAliveCount = 0;

    public NoveltyAliveAverage(int updateRate) {
        super(updateRate);
    }

    @Override
    public double[] getBehaviour() {
        return new double[]{averageAlive / averageAliveCount};
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();

        int tot = 0;
        int al = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                tot++;
                RechargeableEpuck re = (RechargeableEpuck) a;
                if (re.getEnergy() > 0.1) {
                    al++;
                }
            }
        }
        averageAlive += al / tot;
        averageAliveCount++;
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
