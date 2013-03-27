/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation.test;

import evosimbad.aggregation.AggregationClusters;
import evosimbad.aggregation.AggregationClusters.Cluster;
import evosimbad.evolution.MCNSFunction;
import java.util.HashSet;

/**
 *
 * @author Jorge
 */
public class AggregationEvaluation extends MCNSFunction {
    
    private double fitness = 0;
    private double fitnessWCount = 0;
    
    private int finalSteps;
    private double lastDistance = 0;
    private int lastDistanceCount = 0;
    private double acceptableDistance;
    
    private double[] behaviour = null;
    private double clusterThreshold;
    private int index = 0;
    
    public AggregationEvaluation(int updateRate, double clusterThreshold, int finalSteps, double acceptableDistance) {
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
        if(experiment.getCurrentStep() > experiment.getTotalSteps() - finalSteps) {
            lastDistance += dist;
            lastDistanceCount++;
        }
        
        // Behaviour - numero de clusters
        if(behaviour == null) {
            behaviour = new double[getTotalEvaluations()];
        }
        HashSet<Cluster> clusters = AggregationClusters.findAgentClusters(experiment.getEnvironment().getAgents(), clusterThreshold);
        behaviour[index++] = clusters.size();
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
        return - fitness / fitnessWCount;
    }
}
