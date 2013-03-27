package org.neat4j.core.distribute;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;

import org.apache.log4j.Category;
import org.neat4j.core.AIConfig;
import org.neat4j.neat.core.NEATFitnessFunction;

public class ExperimentAgent implements Runnable {
	private static Category cat = Category.getInstance(ExperimentAgent.class);
	private static final int MESSAGE_MARKER = 0x01;
	private static final int COMMAND_REGISTER = 0x10;
	private static final int COMMAND_REQUEST_EXPERIMENT = 0x20;
	private static final int COMMAND_REQUEST_FUNCTION = 0x40;
	private static int PORT = 1969;

	private AIConfig experimentConfig;
	private boolean isWorking = false;
	private Experiment experiment;
	private int id;
	private OutputStream os;
	private InputStream is;
	private String serverUrl;
	private NEATFitnessFunction func;
	private static final char[] ALIVE = new char[] {'/', '-', '\\', '-'};
	private int aliveIdx = 0;
	
	/**
	 * Creates a remote experiment agent that connects to the experiment server located at url
	 * @param url
	 * @throws MalformedURLException
	 */
	public ExperimentAgent(String url) throws MalformedURLException {
		this.serverUrl = url;
	}
	
	/**
	 * Implementation of Runnable
	 */
	public void run() {
		while (true) {
			this.runAgent();
		}
	}
	
	/**
	 * Controller of the agent.  This method runs controls communication with the experiment server and running of the experiment
	 *
	 */
	public void runAgent() {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		try {
			cat.debug("Connecting...");
			this.connectToServer();
			oos = new ObjectOutputStream(this.os);
			ois = new ObjectInputStream(this.is);
			cat.debug("Registering...");
			this.registerExperimentHandler();
			this.readRegistrationResponse();
			this.requestExperimentFunction();
			this.readFunctionResponse(ois);
			cat.info("Registered id:" + this.id);
			while (true) {
				//cat.debug("Request Experiment");
				this.requestExperiment();
				//cat.debug("Read Experiment");
				this.readExperimentResponse(ois);
				//cat.debug("Run Experiment");
				this.runExperiment();
				//cat.debug("Return Experiment");
				this.returnExperimentResult(oos);
			}
		} catch (Exception e) {
			try {
				synchronized (this) {
					this.wait(10000);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
		
	private void connectToServer() throws IOException {
		Socket conn = new Socket(this.serverUrl, PORT);
		this.os = conn.getOutputStream();
		this.is = conn.getInputStream();
		//cat.debug("Got basic streams");
	}

	public void setHandlerId(int id) {
		this.id = id;
	}
	
	public int handlerId() {
		return (this.id);
	}
	
//	public void acceptExperiment(Experiment experiment) {
//		this.isWorking = true;
//		this.experiment = experiment;
//	}
	
	private void registerExperimentHandler() throws IOException {	
		os.write(MESSAGE_MARKER);
		os.write(COMMAND_REGISTER);
		os.write(MESSAGE_MARKER);
	}
	
	private void requestExperiment() throws IOException {	
		os.write(MESSAGE_MARKER);
		os.write(COMMAND_REQUEST_EXPERIMENT);
		os.write(MESSAGE_MARKER);
	}
	
	private void requestExperimentFunction() throws IOException {	
		os.write(MESSAGE_MARKER);
		os.write(COMMAND_REQUEST_FUNCTION);
		os.write(MESSAGE_MARKER);
	}

	private void returnExperimentResult(ObjectOutputStream oos) {
		try {
			oos.writeObject(this.experiment);
			oos.flush();
			//cat.debug(this.id + " Completed Experiment " + this.experiment.id());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//cat.debug("Written experiment " + this.experiment.id());
	}

	private void readExperimentResponse(ObjectInputStream ois) {
		try {
			this.experiment = (Experiment)ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//cat.debug("Read experiment " + this.experiment.id());
	}

	private void readFunctionResponse(ObjectInputStream ois) {
		try {
			this.func = (NEATFitnessFunction)ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//cat.debug("Read experiment " + this.experiment.id());
	}

	private void readRegistrationResponse() throws IOException {
		is.read();
		this.id = is.read();
		//cat.debug("Read id " + this.id);
		is.read();
	}
	
	private void runExperiment() {
//		cat.debug(this.id + " Performing Experiment " + this.experiment.id());
		System.out.print(ALIVE[this.aliveIdx] + "\r");
		aliveIdx = (aliveIdx + 1) % ALIVE.length;
		this.experiment.setupFunction(this.func);
		this.experiment.performExperiment();
		//cat.debug("Performed Experiment");
		this.isWorking = false;
	}
	
	public boolean isAvailable() {
		return (!this.isWorking);
	}	
}
