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
public class NoveltyMovement extends InformedCharacterisation {

    private double averageMovement;
    private int averageMovementCount = 0;

    public NoveltyMovement(int updateRate) {
        super(updateRate);
    }

    @Override
    public double[] getBehaviour() {
        return new double[]{averageMovement / averageMovementCount};
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();

        // measure average speed
        double mov = 0;
        int al = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                if (re.getEnergy() > 0.1) {
                    double speed = Math.abs(re.getKinematics().getLeftVelocity()) + Math.abs(re.getKinematics().getRightVelocity());
                    mov += speed / 2 / Epuck.MAX_WHEEL_VELOCITY;
                    al++;
                }
            }
        }
        if (al > 0) {
            averageMovement += mov / al;
            averageMovementCount++;
        }
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
