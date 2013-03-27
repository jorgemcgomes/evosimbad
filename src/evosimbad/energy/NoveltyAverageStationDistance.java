/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import javax.vecmath.Point3d;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class NoveltyAverageStationDistance extends InformedCharacterisation {

    private double averageDistance;
    private int averageDistanceCount = 0;
    private Point3d p1 = new Point3d(), p2 = new Point3d();

    public NoveltyAverageStationDistance(int sampleRate) {
        super(sampleRate);
    }

    @Override
    public double[] getBehaviour() {
        return new double[]{averageDistance / averageDistanceCount};
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();

        double avgDist = 0;
        int al = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                RechargeableEpuck re = (RechargeableEpuck) a;
                if (re.getEnergy() > 0.1) { // if alive
                    a.getCoords(p1);
                    double minDist = Double.POSITIVE_INFINITY;
                    for (Agent station : agents) {
                        if (station instanceof PointOfCharge) {
                            station.getCoords(p2);
                            minDist = Math.min(minDist, p1.distance(p2));
                        }
                    }
                    al++;
                    avgDist += minDist;
                }
            }
        }
        if(al > 0) {
            averageDistance += avgDist / al / (experiment.getEnvironment().worldSize / 2);
            averageDistanceCount++;
        }
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
