/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

import evosimbad.core.Simulation;
import java.util.ArrayList;
import java.util.HashSet;
import javax.vecmath.Point3d;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class AggregationMovingCenterMass extends AggregationCenterMass {

    protected AggregationCenterMass centerMass;
    private int gridSize;
    private HashSet<Integer> set;
    protected double[] explorationEvolution;
    protected int index = 0;

    public AggregationMovingCenterMass(int updateRate, int gridSize) {
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
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();

        // center of mass
        Point3d centerOfMass = new Point3d();
        Point3d pt = new Point3d();
        for (Agent a : agents) {
            a.getCoords(pt);
            centerOfMass.x += pt.x;
            centerOfMass.y += pt.y;
            centerOfMass.z += pt.z;
        }
        centerOfMass.x /= agents.size();
        centerOfMass.y /= agents.size();
        centerOfMass.z /= agents.size();

        // exploration
        double worldSize = experiment.getEnvironment().worldSize;
        int x = (int) ((centerOfMass.x + worldSize / 2) * gridSize / worldSize);
        int y = (int) ((centerOfMass.y + worldSize / 2) * gridSize / worldSize);
        int i = x * gridSize + y;
        set.add(i);
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
