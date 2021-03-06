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
import javax.vecmath.Point3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class AggregationClusters extends InformedCharacterisation {

    private double fitnessWCount = 0;
    private double fitness = 0;
    private double threshold;
    private double[] behaviour;
    protected int[] clustersEvolution;
    private int index = 0;
    protected int maxAgents;

    public AggregationClusters(int updateRate, double threshold) {
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
        // Initialization - each element is a cluster
        HashSet<Cluster> clusters = new HashSet<>(points.size());
        for (Point3d pt : points) {
            Cluster cluster = new Cluster();
            cluster.elements.add(pt);
            clusters.add(cluster);
        }

        // Clustering
        for (int i = 0; i < points.size(); i++) { // Nunca chega ao fim na verdade
            Pair<Cluster, Cluster> closest = findClosestPossible(clusters, threshold);
            if (closest == null) {
                break;
            } else {
                // Une os clusters em um so
                closest.getLeft().join(closest.getRight());
                clusters.remove(closest.getRight());
            }
        }
        return clusters;
    }

    static private Pair<Cluster, Cluster> findClosestPossible(HashSet<Cluster> clusters, double threshold) {
        Pair<Cluster, Cluster> closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (Cluster c1 : clusters) {
            for (Cluster c2 : clusters) {
                if (c1 != c2) {
                    double dist = c1.distance(c2);
                    if (dist < closestDistance) {
                        closestDistance = dist;
                        closest = new ImmutablePair<>(c1, c2);
                    }
                }
            }
        }
        if (closestDistance <= threshold) {
            return closest;
        }
        return null;
    }

    static class Cluster {

        HashSet<Point3d> elements;

        Cluster() {
            elements = new HashSet<>(20);
        }

        private double distance(Cluster other) {
            double minDistance = Double.POSITIVE_INFINITY;
            for (Point3d pt : this.elements) {
                for (Point3d otherPt : other.elements) {
                    double d = pt.distance(otherPt);
                    if (d < minDistance) {
                        minDistance = d;
                    }
                }
            }
            double weight = 1 + Math.log10(1 + 9
                    * (Math.min(this.elements.size(), other.elements.size())
                    / Math.max(this.elements.size(), other.elements.size())));
            double dist = weight * minDistance;
            return dist;
        }

        private void join(Cluster other) {
            elements.addAll(other.elements);
            other.elements.clear();
        }
    }

    public static void main(String[] args) {
        ArrayList<Point3d> points = new ArrayList<>();
        points.add(new Point3d(1, 0, 1));
        points.add(new Point3d(1.05, 0, 1.05));
        points.add(new Point3d(1.2, 0, 1.2));
        points.add(new Point3d(0, 0, 0));
        points.add(new Point3d(0.5, 0, 0.5));
        points.add(new Point3d(0.6, 0, 0.6));
        points.add(new Point3d(2, 0, 2));
        points.add(new Point3d(-1, 0, -1));
        HashSet<Cluster> findClusters = findClusters(points, 0.3);
        for (Cluster cl : findClusters) {
            System.out.println("CLUSTER");
            for (Point3d pt : cl.elements) {
                System.out.println(pt);
            }
        }
    }
}
