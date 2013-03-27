/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

import evosimbad.deprecated.aggregation.AggregationClusters2.Cluster;
import evosimbad.core.Simulation;
import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import java.util.HashSet;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class AggregationBiggestClusterBehaviour extends InformedCharacterisation {

    protected AggregationCenterMass centerMass;
    private double[] behaviour;
    private int index = 0;
    private double clusterThreshold;

    public AggregationBiggestClusterBehaviour(int updateRate, double clusterThreshold) {
        super(updateRate);
        centerMass = new AggregationCenterMass(updateRate);
        this.clusterThreshold = clusterThreshold;
    }

    @Override
    public void setSimulatorExperiment(Simulation exp) {
        super.setSimulatorExperiment(exp);
        centerMass.setSimulatorExperiment(exp);
        behaviour = new double[super.getTotalEvaluations()];
    }

    @Override
    public void step() {
        super.step();
        centerMass.step();
    }

    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
        HashSet<Cluster> clusters = AggregationClusters2.findAgentClusters(agents, clusterThreshold);
        int biggest = 0;
        for(Cluster c : clusters) {
            if(c.size() > biggest) {
                biggest = c.size();
            }
        }
        behaviour[index++] = (biggest - 1) / (double) (agents.size());
    }

    @Override
    public double getFitness() {
        return centerMass.getFitness();
    }

    @Override
    public double[] getBehaviour() {
        return behaviour;
    }
}
