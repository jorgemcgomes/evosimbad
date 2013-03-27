/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation;

import evosimbad.aggregation.AggregationClusters.Cluster;
import evosimbad.core.EvaluationFunction;
import java.util.ArrayList;
import java.util.HashSet;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class AggregationClustersFitness extends EvaluationFunction {
    
    private double fitness;
    
    @Override
    public void evaluate() {
        if(super.getCurrentEvaluation() == super.getTotalEvaluations()) {
            ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
            HashSet<Cluster> clusters = AggregationClusters.findAgentClusters(agents, 0.25);
            fitness = (agents.size() - clusters.size()) / (double) (agents.size() - 1);
        }
    }

    @Override
    public double getFitness() {
        return fitness;
    }
    
    
    
}
