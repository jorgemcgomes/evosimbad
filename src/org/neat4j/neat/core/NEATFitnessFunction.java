/*
 * Created on 22-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;

import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.NeuralFitnessFunction;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 * Provides common behaviour for all NEAT Fitness functions which should all extnd this class 
 * @author MSimmerson
 *
 */
public abstract class NEATFitnessFunction extends NeuralFitnessFunction {
	/**
	 * @param net
	 * @param dataSet
	 */
	public NEATFitnessFunction(NeuralNet net, NetworkDataSet dataSet) {
		super(net, dataSet);
	}
	
	public void createNetFromChromo(Chromosome genoType) {
		((NEATNetDescriptor)this.net().netDescriptor()).updateStructure(genoType);
		((NEATNeuralNet)this.net()).updateNetStructure();
	}

	public int requiredChromosomeSize() {
		return 0;
	}
}
