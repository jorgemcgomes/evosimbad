/*
 * Created on 22-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core.control;

import java.lang.reflect.Constructor;

import org.apache.log4j.Category;
import org.neat4j.core.AIConfig;
import org.neat4j.core.AIController;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.data.csv.CSVDataLoader;
import org.neat4j.neat.nn.core.Learnable;
import org.neat4j.neat.nn.core.LearningEnvironment;
import org.neat4j.neat.nn.core.NeuralNet;
import org.neat4j.neat.nn.core.NeuralNetDescriptor;
import org.neat4j.neat.nn.core.NeuralNetFactory;

/**
 * @author MSimmerson
 *
 * Mamages the initialisation and creation of a NEAT network
 */
public class NEATNetManager implements AIController {
	private static final Category cat = Category.getInstance(NEATNetManager.class);
	private NeuralNet net;
	private AIConfig config;

	public boolean save(String fileName) {
		return (false);
	}

	public NeuralNetDescriptor createNetDescriptor() {
		return (this.createNetDescriptor(this.config));
	}

	public NeuralNetDescriptor createNetDescriptor(AIConfig config) {
		NeuralNetDescriptor descriptor = null;
		int inputLayerSize = Integer.parseInt(config.configElement("INPUT_SIZE"));
		int outputLayerSize = Integer.parseInt(config.configElement("OUTPUT_SIZE"));
		// create learnable
		Learnable learnable = this.createLearnable(config, outputLayerSize);
		descriptor = new NEATNetDescriptor(inputLayerSize, learnable);
		return (descriptor);
	}

	public void initialise(AIConfig config) throws InitialisationFailedException {
		this.config = config;
		NeuralNetDescriptor descriptor = this.createNetDescriptor();
		this.net = NeuralNetFactory.getFactory().createNN(descriptor);
	}	
	
	public NetworkDataSet dataSet(String keyName, AIConfig config, int opSize) {
		NetworkDataSet dSet = null;
		String fileName = config.configElement(keyName);
		if (fileName != null) {
			dSet = new CSVDataLoader(fileName, opSize).loadData();			
		}
	
	return (dSet);
}

	public Learnable createLearnable(AIConfig config, int numOutputs) {
		Learnable learnable = null;
		String learnableClassName = config.configElement("LEARNABLE");
		if (learnableClassName != null) {
			cat.debug("learnableClassName:" + learnableClassName);
			// learning env
			LearningEnvironment le = new LearningEnvironment();
			NetworkDataSet dSet = this.dataSet("TRAINING.SET", config, numOutputs);
			le.addEnvironmentParameter("TRAINING.SET", dSet);
			try {
				Class lClass = Class.forName(learnableClassName);
				Constructor c = lClass.getConstructor(new Class[]{LearningEnvironment.class});
				learnable = (Learnable) c.newInstance(new Object[]{le});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return (learnable);
	}	
	
	public NeuralNet managedNet() {
		return (this.net);
	}
}
