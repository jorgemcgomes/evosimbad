/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.mo;

import org.neat4j.neat.core.NEATFitnessFunction;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 *
 * @author jorge
 */
public abstract class MultiObjectiveFitnessFunction extends NEATFitnessFunction {

    public MultiObjectiveFitnessFunction(NeuralNet net, NetworkDataSet dataSet) {
        super(net, dataSet);
    }
    
    public abstract double[] evaluateObjectives(Chromosome genoType);

    @Override
    public double evaluate(Chromosome genoType) {
        return 0;
    }
}
