/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.commons.Epuck;
import evosimbad.core.EvaluationFunction;
import java.util.ArrayList;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class FitnessFloreano extends EvaluationFunction {
    
    private double fitness = 0;

    public FitnessFloreano(int updateInterval) {
        super(updateInterval);
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
        int size = 0;
        double val = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                size++;
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
        fitness += val / size;
    }

    @Override
    public double getFitness() {
        return fitness / getTotalEvaluations();
    }
}
