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
 * @author jorge
 */
public class NoveltyFloreano extends InformedCharacterisation {

    private int total = 0;
    private double[] behav;

    public NoveltyFloreano(int updateRate) {
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

        // novelty measure
        double val = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                double wheelVel = Math.abs(re.getKinematics().getLeftVelocity() + re.getKinematics().getRightVelocity()) / 2;
                wheelVel /= Epuck.MAX_WHEEL_VELOCITY;
                
                double[] obstacleIR = re.getObstacleIR().getQuadrantsMeasurements();
                double minDistance = Double.POSITIVE_INFINITY;
                for(double d : obstacleIR) {
                    minDistance = Math.min(d, minDistance);
                }
                minDistance = Double.isInfinite(minDistance) ? 1 : minDistance / re.getObstacleIR().getMaxRange();

                val += wheelVel * minDistance;
            }
        }
        behav[getCurrentEvaluation() - 1] = val / total;
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
