/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.simulation;

import evosimbad.core.EvaluationFunction;
import evosimbad.core.Simulator;
import evosimbad.core.Simulation;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.encog.ml.MLRegression;
import simbad.gui.Simbad;

/**
 *
 * @author Jorge
 */
public class DebugSimulator extends Simulator {
    
    @Override
    public HashMap<MLRegression, List<EvaluationFunction>> runExperiments(Set<MLRegression> controllers) {
        MLRegression next = controllers.iterator().next();
        List<Simulation> exps = simBuilder.prepareExperiments(next);
        Simbad sim = new Simbad(exps.get(0).getEnvironment(), false, 30);
        return null;
    }
    
}
