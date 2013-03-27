/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

import evosimbad.core.Simulation;
import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.vecmath.Point3d;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class AggregationClusters2 extends InformedCharacterisation {

    private double fitnessWCount = 0;
    private double fitness = 0;
    private double threshold;
    private double[] behaviour;
    protected int[] clustersEvolution;
    private int index = 0;
    protected int maxAgents;

    public AggregationClusters2(int updateRate, double threshold) {
        super(updateRate);
        this.threshold = threshold;
    }

    @Override
    public void setSimulatorExperiment(Simulation exp) {
        super.setSimulatorExperiment(exp);
        maxAgents = exp.getEnvironment().getAgents().size();
    }
    
    @Override
    public void evaluate() {
        double w = experiment.getCurrentStep() / (double) experiment.getTotalSteps();
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
        int clusterSize = findAgentClusters(agents, threshold).size();
        double clusters = (clusterSize - 1) / (double) (maxAgents - 1); // normalizes between 0 (min clusters) and 1 (max clusters)
        fitness += clusters * w;
        fitnessWCount += w;
        if (behaviour == null) {
            behaviour = new double[getTotalEvaluations()];
            clustersEvolution = new int[behaviour.length];
        }
        clustersEvolution[index] = clusterSize;
        behaviour[index++] = clusters;
    }
    
    @Override
    public double getFitness() {
        return 1 - (fitness / fitnessWCount);
    }

    @Override
    public double[] getBehaviour() {
        return behaviour;
    }

    static HashSet<Cluster> findAgentClusters(ArrayList<Agent> agents, double threshold) {
        ArrayList<Point3d> points = new ArrayList<>(agents.size());
        for (Agent a : agents) {
            Point3d point = new Point3d();
            a.getCoords(point);
            points.add(point);
        }
        return findClusters(points, threshold);
    }

    /*
     * Bottom up hierarchical clustering
     */
    static private HashSet<Cluster> findClusters(Collection<Point3d> points, double threshold) {
        HashSet<Cluster> clusters = new HashSet<>(points.size() * 2);
        for(Point3d p : points) {
            Cluster candidate = new Cluster();
            candidate.add(p);
            for(Point3d p2 : points) {
                if(p != p2 && p.distance(p2) <= threshold) {
                    candidate.add(p2);
                }
            }
            Iterator<Cluster> iter = clusters.iterator();
            while(iter.hasNext()) {
                Cluster c = iter.next();
                for(Point3d p2 :candidate) {
                    if(c.contains(p2)) { // Se os clusters C e candidate se intersectam, fundi-los
                        candidate.addAll(c);
                        iter.remove();
                        break;
                    }
                }
            }
            clusters.add(candidate);
        }
        return clusters;
    }

    static class Cluster extends HashSet<Point3d> {
        Cluster() {
            super(20);
        }
    }
}
