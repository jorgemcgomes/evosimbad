/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.simulation;

import evosimbad.core.EvaluationFunction;
import evosimbad.core.NNAgent;
import evosimbad.core.SimulationBuilder;
import evosimbad.core.Simulation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.commons.lang3.tuple.Pair;
import org.neat4j.neat.nn.core.NeuralNet;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author Jorge
 */
public class SimpleSimulationBuilder extends SimulationBuilder {
    
    private int steps;
    private int fps;
    private int samples;
    
    public SimpleSimulationBuilder(int steps, int fps, int samples) {
        this.steps = steps;
        this.samples = samples;
        this.fps = fps;
    }

    @Override
    public List<Simulation> prepareExperiments(NeuralNet controller) {
        List<Simulation> exps = new ArrayList<>(samples);
        for(int i = 0 ; i < samples ; i++) {
            EnvironmentDescription env = envGenerator.generateEnvironment();
            Set<NNAgent> agents = agentGenerator.generateAgents();
            List<Pair<Point3d, Double>> positions = agentPlacer.generateStartPositions(env, agents.size());
            Iterator<Pair<Point3d, Double>> iterator = positions.iterator();
            for (NNAgent ag : agents) {
                ag.setController(controller);
                Pair<Point3d, Double> position = iterator.next();
                ag.setStartPosition(new Vector3d(position.getLeft()));
                ag.setInitialRotation(position.getRight());
                env.add(ag);
            }
            EvaluationFunction ef = createEvaluationFunction();
            Simulation exp = new Simulation(env, steps, fps, ef);
            exps.add(exp);
        }
        return exps; 
    }
    
    public void setSamples(int samples) {
        this.samples = samples;
    }
}
