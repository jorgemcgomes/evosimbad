/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.neat4j.neat.nn.core.NeuralNet;
import simbad.gui.Simbad;

/**
 *
 * @author Jorge
 */
public abstract class Simulator {
    
    protected SimulationBuilder simBuilder;
    
    void setSimulationBuilder(SimulationBuilder simBuilder) {
        this.simBuilder = simBuilder;
    }

    public abstract HashMap<NeuralNet, List<EvaluationFunction>> runExperiments(Set<NeuralNet> controllers);
    
    
    public void showIndividual(NeuralNet controller) {
        List<Simulation> prepareExperiments = simBuilder.prepareExperiments(controller);
        for (Simulation exp : prepareExperiments) {
            new Simbad(exp.getEnvironment(), false, exp.getFPS());
        }
    }
}
