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
 * @author jorge
 */
public class NoveltyStationDistance extends InformedCharacterisation {

    private int total = 0;
    private double[] behav;
    private double normalization;
    private Point3d p1 = new Point3d(), p2 = new Point3d();

    public NoveltyStationDistance(int updateRate, double normalization) {
        super(updateRate);
        this.normalization = normalization;
    }
    
    public NoveltyStationDistance(int updateRate) {
        super(updateRate);
        this.normalization = 0;
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
            if(normalization == 0) {
                normalization = experiment.getEnvironment().worldSize / 2;
            }
        }

        // novelty measure
        double avgDist = 0;
        for (Agent a : agents) {
            if (a instanceof RechargeableEpuck) {
                a.getCoords(p1);
                double minDist = Double.POSITIVE_INFINITY;
                for(Agent station : agents) {
                    if(station instanceof PointOfCharge) {
                        station.getCoords(p2);
                        minDist = Math.min(minDist, p1.distance(p2));
                    }
                }
                avgDist += minDist;
            }
        }
        behav[getCurrentEvaluation() - 1] = avgDist / total / normalization;
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
