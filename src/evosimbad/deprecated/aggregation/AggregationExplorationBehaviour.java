/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

import evosimbad.core.Simulation;
import evosimbad.evolution.InformedCharacterisation;
import java.util.HashSet;
import javax.vecmath.Point3d;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class AggregationExplorationBehaviour extends InformedCharacterisation {

    protected AggregationCenterMass centerMass;
    private int gridSize;
    private HashSet<Integer> set;
    protected double[] explorationEvolution;
    protected int index = 0;

    public AggregationExplorationBehaviour(int updateRate, int gridSize) {
        super(updateRate);
        centerMass = new AggregationCenterMass(updateRate);
        this.gridSize = gridSize;
    }

    @Override
    public void setSimulatorExperiment(Simulation exp) {
        super.setSimulatorExperiment(exp);
        centerMass.setSimulatorExperiment(exp);
        this.set = new HashSet<>(gridSize * gridSize * 2);
        this.explorationEvolution = new double[super.getTotalEvaluations()];
    }

    @Override
    public void step() {
        super.step();
        centerMass.step();
        double worldSize = experiment.getEnvironment().worldSize;
        Point3d p = new Point3d();
        for(Agent ag : experiment.getEnvironment().getAgents()) {
            ag.getCoords(p);
            int x = (int) ((p.x + worldSize / 2) * gridSize / worldSize);
            int y = (int) ((p.y + worldSize / 2) * gridSize / worldSize);
            int i = x * gridSize + y;
            set.add(i);
        }        
    }

    @Override
    public void evaluate() {
        //System.out.print(set.size() + " " + experiment.getEnvironment().getAgents().size());
        double value = (double) set.size() /*/ (gridSize * gridSize)*/ / experiment.getEnvironment().getAgents().size();
        explorationEvolution[index++] = value;
        set.clear();
    }

    @Override
    public double getFitness() {
        return centerMass.getFitness();
    }

    @Override
    public double[] getBehaviour() {
        return explorationEvolution;
    }
}
