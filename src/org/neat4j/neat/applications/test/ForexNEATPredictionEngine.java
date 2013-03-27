package org.neat4j.neat.applications.test;

import java.io.IOException;

import org.apache.log4j.Category;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.applications.core.ApplicationEngine;
import org.neat4j.neat.applications.core.NEATApplicationEngine;
import org.neat4j.neat.applications.gui.NEATFrame;
import org.neat4j.neat.core.NEATLoader;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.control.NEATNetManager;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkInputSet;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.csv.CSVDataLoader;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

public class ForexNEATPredictionEngine extends NEATApplicationEngine {
	private static final Category cat = Category.getInstance(NEATApplicationEngine.class);

	private void showNet() {
		NEATFrame frame = new NEATFrame((NEATNeuralNet)this.net());
		frame.showNet();
	}

	public void initialise(AIConfig config) throws InitialisationFailedException {
		String aiSource = config.configElement("AI.SOURCE");
		// what type is the AI?
		String aiType = config.configElement("AI.TYPE");
		try {
			if (GA.equalsIgnoreCase(aiType)) {
				Chromosome chromo = (Chromosome)this.readObject(aiSource);
				// need to create a nn based on this chromo.
				this.setNet(this.createNet(config));
				((NEATNetDescriptor)(this.net().netDescriptor())).updateStructure(chromo);
				((NEATNeuralNet)this.net()).updateNetStructure();
				this.showNet();
			} else {
				throw new InitialisationFailedException("Illegal AI Type:" + aiType);
			}
			
			// now setup the input data
			String dataFile = config.configElement("INPUT.DATA");
			if (dataFile != null) {
				this.setNetData(new CSVDataLoader(dataFile, 0).loadData());
			}
		} catch (IOException e) {
			throw new InitialisationFailedException("Problem loading " + aiSource + ":" + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new InitialisationFailedException("Cannot find class for " + aiSource + ":" + e.getMessage());
		} catch (ClassCastException e) {
			throw new InitialisationFailedException("Incompatable AI source and type" + aiSource + ":" + aiType);
		}
	}

	public NeuralNet createNet(AIConfig config) throws InitialisationFailedException {
		String nnConfigFile;
		AIConfig nnConfig;
		NEATNetManager netManager;
		
		nnConfigFile = config.configElement("NN.CONFIG");
		nnConfig  = new NEATLoader().loadConfig(nnConfigFile);
		nnConfig.updateConfig("INPUT_SIZE", config.configElement("INPUT.NODES"));
		nnConfig.updateConfig("OUTPUT_SIZE", config.configElement("OUTPUT.NODES"));
		netManager = new NEATNetManager();
		netManager.initialise(nnConfig);
		
		return ((NEATNeuralNet)netManager.managedNet());
	}

	public void runApplication() {
		NetworkDataSet dataSet = this.netData();
		NetworkInputSet ipSet = dataSet.inputSet();
		NetworkInput ip;
		NetworkOutputSet opSet = null;
		int i;
		 
		for (i = 0; i < ipSet.size(); i++) {
			ip = ipSet.inputAt(i);
			opSet = this.net().execute(ip);
			if (i == 431) {
				System.out.println("*********************");
			}
			System.out.println(opSet.nextOutput().values()[0]);
		}		
	}

	public static void main(String[] args) {
		ApplicationEngine fpe = new ForexNEATPredictionEngine();
		AIConfig config = new NEATLoader().loadConfig(args[0]);
		try {
			fpe.initialise(config);
			fpe.runApplication();
		} catch (InitialisationFailedException e) {
			cat.error("Failed to initialise ForexNEATPredictionEngine:" + e.getMessage());
		}
	}

}
