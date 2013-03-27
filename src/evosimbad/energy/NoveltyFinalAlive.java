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
public class NoveltyFinalAlive extends InformedCharacterisation {

    private double alive;

    public NoveltyFinalAlive() {
        super(1);
    }

    @Override
    public double[] getBehaviour() {
        return new double[]{alive};
    }

    @Override
    public void evaluate() {

        // alive at the end
        int tot = 0;
        if (super.getCurrentEvaluation() == super.getTotalEvaluations()) {
            ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
            for (Agent a : agents) {
                if (a instanceof RechargeableEpuck) {
                    tot++;
                    RechargeableEpuck re = (RechargeableEpuck) a;
                    if (re.getEnergy() > 0.1) {
                        alive += 1;
                    }
                }
            }
            alive = alive / tot;        
        }
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
