/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation.test;

import evosimbad.aggregation.AggregationClusters;
import evosimbad.aggregation.AggregationClusters.Cluster;
import evosimbad.evolution.MCNSFunction;
import java.util.ArrayList;
import java.util.HashSet;
import javax.vecmath.Point3d;

/**
 *
 * @author Jorge
 */
public class AggregationEvaluation2 extends MCNSFunction {

    private double fitness = 0;
    private double fitnessWCount = 0;
    private int finalSteps;
    private double lastDistance = 0;
    private int lastDistanceCount = 0;
    private double acceptableDistance;
    private double[] behaviour = null;
    private double clusterThreshold;
    private int index = 0;

    public AggregationEvaluation2(int updateRate, double clusterThreshold, int finalSteps, double acceptableDistance) {
        super(updateRate);
        this.clusterThreshold = clusterThreshold;
        this.finalSteps = finalSteps;
        this.acceptableDistance = acceptableDistance;
    }

    @Override
    public void evaluate() {
        // Fitness - media ponderada, quanto mais para a frente mais importancia tem
        double dist = CenterMassEvaluation.averageDistanceToCenterMass(experiment.getEnvironment().getAgents());
        double w = experiment.getCurrentStep() / (double) experiment.getTotalSteps();
        fitnessWCount += w;
        fitness += dist * w;

        // Minimal criteria - no final tem que estar minimamente agregados
        if (experiment.getCurrentStep() > experiment.getTotalSteps() - finalSteps) {
            lastDistance += dist;
            lastDistanceCount++;
        }

        // Behaviour - numero de clusters
        if (behaviour == null) {
            behaviour = new double[getTotalEvaluations() * 2];
        }
        HashSet<Cluster> clusters = AggregationClusters.findAgentClusters(experiment.getEnvironment().getAgents(), clusterThreshold);
        behaviour[index++] = clusters.size() / experiment.getEnvironment().getAgents().size();
        behaviour[index++] = averageInterClusterDistance(clusters) / experiment.getEnvironment().worldSize;
    }

    @Override
    public boolean meetsMinimalCriteria() {
        return lastDistance / lastDistanceCount < acceptableDistance;
    }

    @Override
    public double[] getBehaviour() {
        return behaviour;
    }

    @Override
    public double getFitness() {
        return -fitness / fitnessWCount;
    }

    private double averageInterClusterDistance(HashSet<Cluster> clusters) {
        if(clusters.size() == 1) {
            return 0;
        }
        
        // find clusters centroids
        ArrayList<Point3d> centroids = new ArrayList<>(clusters.size());
        for (Cluster c : clusters) {
            int size = c.elements.size();
            Point3d centroid = new Point3d(0, 0, 0);
            for (Point3d e : c.elements) {
                centroid.x += e.x / size;
                centroid.y += e.y / size;
                centroid.z += e.z / size;
            }
            centroids.add(centroid);
        }

        double avg = 0;
        for (Point3d p : centroids) {
            for (Point3d p2 : centroids) {
                if (p != p2) {
                    avg += p.distance(p2);
                }
            }
        }
        avg /= (centroids.size() * (centroids.size() - 1));

        return avg;
    }
}
