/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.simulation;

import evosimbad.core.EvaluationFunction;
import evosimbad.core.SimulationBuilder;
import evosimbad.core.Simulator;
import evosimbad.core.Simulation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.encog.ml.MLRegression;
import simbad.gui.Simbatch;

/**
 *
 * @author Jorge
 */
public class SingleThreadedSimulator extends Simulator {
    
    public SingleThreadedSimulator() {
        super();
    }

    @Override
    public HashMap<MLRegression, List<EvaluationFunction>> runExperiments(Set<MLRegression> controllers) {
        HashMap<MLRegression, List<EvaluationFunction>> result = new HashMap<>();
        System.out.println();
        for(MLRegression controller : controllers) {
            List<Simulation> exps = simBuilder.prepareExperiments(controller);
            List<EvaluationFunction> evals = new ArrayList<>(exps.size());            
            for(Simulation exp : exps) {
                Simbatch sim = new Simbatch(exp.getEnvironment());
                sim.simulator.setFramesPerSecond(exp.getFPS());
                sim.reset();
                for(int i = 0 ; i < exp.getTotalSteps() ; i++) {
                    exp.step();                    
                    sim.step();
                }
                exp.done();
                sim.dispose();
                evals.add(exp.getEvaluationFunction());
            }
            result.put(controller, evals);
            System.out.print(".");
        }              
        return result;
    }
}
