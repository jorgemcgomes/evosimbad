package org.neat4j.neat.applications.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 * Provides bare bones NEAT application 
 * @author MSimmerson
 *
 */
public abstract class NEATApplicationEngine implements ApplicationEngine {
	protected static final String GA = "GA"; 
	private static final String NN = "NN"; 
	private NeuralNet net;
	private NetworkDataSet netData;

	/**
	 * @see org.neat4j.ailibrary.core.AIController#initialise(org.neat4j.ailibrary.core.AIConfig)
	 */
	public abstract void initialise(AIConfig config) throws InitialisationFailedException;
	public abstract NeuralNet createNet(AIConfig config) throws InitialisationFailedException;
	
	/**
	 * Reads a saved chromosome from fileName
	 * @param fileName - location of chromosome
	 * @return Recreate object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected Object readObject(String fileName) throws IOException, ClassNotFoundException {
		Object o = null;
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		o = ois.readObject();
		ois.close();
		fis.close();
		
		return (o);
	}	 


	public NeuralNet net() {
		return (this.net);
	}
	
	public NetworkDataSet netData() {
		return (this.netData);
	}
	
	public void setNetData(NetworkDataSet netData) {
		this.netData = netData;
	}
	
	public void setNet(NeuralNet net) {
		this.net = net;
	}
}
