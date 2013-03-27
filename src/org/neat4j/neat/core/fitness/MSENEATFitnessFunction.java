package org.neat4j.neat.core.fitness;

import org.neat4j.neat.core.NEATFitnessFunction;
import org.neat4j.neat.data.core.ExpectedOutputSet;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

public class MSENEATFitnessFunction extends NEATFitnessFunction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MSENEATFitnessFunction(NeuralNet net, NetworkDataSet dataSet) {
		super(net, dataSet);
	}

	public double evaluate(Chromosome genoType) {
		int i;
		int j;
		NetworkOutputSet opSet;
		NetworkInput ip;
		ExpectedOutputSet eOpSet = this.evaluationData().expectedOutputSet();
		double[] op;
		double[] eOp;
		double error = 0;

		
		// need to create a net based on this chromo
		this.createNetFromChromo(genoType);
		
		// execute net over data set
		for (i = 0; i < eOpSet.size(); i++) {
			ip = this.evaluationData().inputSet().nextInput();
			opSet = this.net().execute(ip);
			op = opSet.nextOutput().values();
			eOp = eOpSet.nextOutput().values();
			for (j = 0; j < op.length; j++) {
				error += Math.pow(eOp[j] - op[j], 2);
			}
		}
		
		return (Math.sqrt(error / eOpSet.size()));
	}
}
