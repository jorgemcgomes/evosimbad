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
public class NoveltyMovementVector extends InformedCharacterisation {

    private int total = 0;
    private double[] behav;

    public NoveltyMovementVector(int updateRate) {
        super(updateRate);
    }

    @Override
    public double[] getBehaviour() {
        return behav;
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

        // measure movement
        double movement = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                double speed = Math.abs(re.getKinematics().getLeftVelocity()) + Math.abs(re.getKinematics().getRightVelocity());
                speed = speed / 2 / Epuck.MAX_WHEEL_VELOCITY;
                movement += speed / total;
            }
        }
        
        behav[getCurrentEvaluation() - 1] = movement;
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
