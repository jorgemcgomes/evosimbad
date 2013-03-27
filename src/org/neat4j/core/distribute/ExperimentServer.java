package org.neat4j.core.distribute;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Category;
import org.neat4j.core.AIConfig;
import org.neat4j.core.AIController;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.applications.train.NEATGATrainingManager;
import org.neat4j.neat.core.NEATChromosome;
import org.neat4j.neat.core.NEATGeneticAlgorithm;
import org.neat4j.neat.core.NEATLoader;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.FitnessFunction;
import org.neat4j.neat.ga.core.Population;

public class ExperimentServer implements AIController {
	private static final Category cat = Category.getInstance(ExperimentServer.class);
	private static int handlerId = 1;
	private Experiment[] experiments;
	private Experiment[] finishedExperiments;
	private FitnessFunction func;
	private Population pop;
	private static int expIdx = 0;
	private static int expFinishedIdx = 0;
	private NEATGATrainingManager tm;
	private int genNum = 1;
	private ArrayBlockingQueue expQ;
	
	/**
	 * Creates the server socket so incoming connections can be accepted
	 *
	 */
	public void createServer() {
		try {
			ServerSocket server = new ServerSocket(1969);
			while (true) {
				this.acceptHandlerRegistrations(server);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Provides the behaviour for creating incoming connection handling.  Creates a new thread for each incoming connection
	 * @param server
	 * @throws IOException
	 */
	private void acceptHandlerRegistrations(ServerSocket server) throws IOException {
		Socket socket = server.accept();
		ExperimentConnection eConn = new ExperimentConnection(socket, handlerId++, this);
		Thread t = new Thread(eConn);
		t.setName("Handler " + eConn.id());
		t.start();
	}
	
	/**
	 * Gets the next experiment from the queue, if there are none left for
	 * the epoch, it will wait until the next epoch starts.
	 * @return Experiment
	 */
	public synchronized Experiment createExperiment() {
		Experiment exp = null;
		if (expIdx >= this.experiments.length) {
			try {
				//cat.debug(Thread.currentThread().getName() + " is waiting");
				this.wait();
				//cat.debug(Thread.currentThread().getName() + " is active");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			exp = (Experiment)this.expQ.take();
			if (exp != null) {
				//cat.debug("assigning experiment " + exp.id() + ":" + expIdx);
				expIdx++;
			} else {
				cat.error("Queued Experiment was null");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (exp);
	}
	
	/**
	 * If a remote Experiment Agent fails to complete an experiment, it must be put back on the queue
	 * to be re-assigned
	 * @param e - failed experiment
	 */
	public synchronized void returnFailedExperimentToPool(Experiment e) {
		if (e != null) {
			try {
				this.expQ.put(e);
				expIdx--;
				this.notifyAll();
				cat.info("Failed experiment " + e.id() + ":" + expIdx);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		this.notifyAll();
		
	}
	
	/**
	 * When an agent finishes an experiment, experiment stats are updated.  If all experiments for an epcoh are complete
	 * an evolution cycle is initiated.
	 * @param e
	 * @throws ExperimentNotRunException
	 */
	public synchronized void finishedExperiment(Experiment e) throws ExperimentNotRunException {
		if (e != null) {
			this.finishedExperiments[e.id()] = e;
			expFinishedIdx++;
			//cat.debug("Experiment completed = " + e.id() + ":" + expFinishedIdx);
			if (expFinishedIdx >= this.pop.genoTypes().length) {
				this.notifyAll();
				// generation finished;
				cat.info("Finished epoch experiments");
				this.runEvolutionCycle();
				this.createExperiments();
				expFinishedIdx = 0;
				expIdx = 0;
			}
		} else {
			cat.warn("Tried to complete experiment that was null");
		}
	}
	
	private void runEvolutionCycle() throws ExperimentNotRunException {
		Chromosome[] newGenoTypes = new NEATChromosome[this.pop.genoTypes().length];
		cat.info("Finished generation " + this.genNum++);
		int  i;
		for (i = 0; i < newGenoTypes.length; i++) {
			newGenoTypes[i] = this.finishedExperiments[i].result();
		}
		((NEATGeneticAlgorithm)(this.tm.ga())).runEvolutionCycle(newGenoTypes);
		this.tm.saveBest();
		this.pop = this.tm.ga().population();
	}
	
	private void createExperiments()  {
		int i;
		Experiment e;
		// ensure the pool is empty
		this.expQ.clear();
		for (i = 0; i < experiments.length; i++) {
			e = new Experiment(i);
			e.setupExperiment(pop.genoTypes()[i]);
			this.experiments[i] = e;
			try {
				this.expQ.put(e);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		//expIdx = 0;
	}

	/**
	 * Intialises the server environment
	 */
	public void initialise(AIConfig config) throws InitialisationFailedException {
		this.tm = new NEATGATrainingManager();
		this.tm.initialise(config);
		this.pop = tm.ga().population();
		this.experiments = new Experiment[this.pop.genoTypes().length];
		this.expQ = new ArrayBlockingQueue(this.experiments.length);
		this.finishedExperiments = new Experiment[this.pop.genoTypes().length];
		this.func = ((NEATGeneticAlgorithm)tm.ga()).gaEvaluator();
		this.createExperiments();
	}
	
	/**
	 * Makes the relevant fitness function available
	 * @return
	 */
	public FitnessFunction function() {
		return (this.func);
	}
	
	public static void main(String[] args) {
		AIConfig config = new NEATLoader().loadConfig(args[0]);
		ExperimentServer es = new ExperimentServer();
		try {
			es.initialise(config);
			es.createServer();
		} catch (InitialisationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/**
 * Monitors experiments that take too long and cancels any that do.
 * @author MSimmerson
 *
 */
class MonitorTask extends TimerTask {
	private static final Category cat = Category.getInstance(MonitorTask.class);
	private ExperimentConnection conn;
	private Experiment exp;
	
	/**
	 * Creates monitor for an experiment
	 * @param conn
	 * @param exp
	 */
	public MonitorTask(ExperimentConnection conn, Experiment exp) {
		this.conn = conn;
		this.exp = exp;
		if (this.conn == null || this.exp == null) {
			throw new IllegalArgumentException("Illegal values when setting up monior:" + this.conn + ":" + this.exp);
		}
	}
	
	/**
	 * Monitor task implementation.  Only runs if the monitor expires
	 */
	public void run() {
		cat.info("Cancelling experiment:" + this.exp.id() + " using conn " + this.conn.id());
		this.conn.cancelExperiment(this.exp);
		this.conn.allowResult(false);
	}
	
}

/**
 * Handles communications with one remote experiment agent
 * @author MSimmerson
 *
 */
class ExperimentConnection implements Runnable {
	private static final Category cat = Category.getInstance(ExperimentConnection.class);
	private static final int INIT = 0;
	private static final int REGISTERING = 1;
	private static final int WAITING_ON_EXPERIMENT_REQUEST = 2;
	private static final int WAITING_ON_EXPERIMENT_RESULT = 3;
	private static final int WAITING_ON_FUNCTION_REQUEST = 4;
	private static final int FINISHED = 5;
//	private static final int START_READ = 0;
//	private static final int COMMAND_READ = 1;
//	private static final int END_READ = 2;

	private static final int MESSAGE_MARKER = 0x01;
	private static final int COMMAND_REGISTER = 0x10;
	private static final int COMMAND_REQUEST_EXPERIMENT = 0x20;
	private static final int COMMAND_REQUEST_FUNCTION = 0x40;
	private static final int COMMAND_INVALID = 0x00;
	
	private Socket handlerConnection;
	private int id;
	private ExperimentServer server;
	private boolean resultAccepted = true;

	/**
	 * 
	 * @param handlerConnection - physical socket connection
	 * @param id - connection id
	 * @param server - callback
	 */
	public ExperimentConnection(Socket handlerConnection, int id, ExperimentServer server) {
		this.handlerConnection = handlerConnection;
		this.id = id;
		this.server = server;
	}
	
	/**
	 * Cancels failed experiment
	 * @param e
	 */
	public void cancelExperiment(Experiment e) {	
		this.server.returnFailedExperimentToPool(e);
	}
	
	public int id() {
		return (this.id);
	}
	
	/**
	 * Implementation of Runnable
	 */
	public void run() {
		this.stateMachine();		
	}
	
	/**
	 * Sets up variable to allow/disallow the response from the remote experiment agent.
	 * @param allow
	 */
	public void allowResult(boolean allow) {
		this.resultAccepted = allow;
	}
	
	private void stateMachine() {
		int state = INIT;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		int command;
		Experiment exp =  null;
		Timer monitor = null;
		
		try {
			dis = new DataInputStream(this.handlerConnection.getInputStream());
			dos = new DataOutputStream(this.handlerConnection.getOutputStream());
			ois = new ObjectInputStream(dis);
			oos = new ObjectOutputStream(dos);

			while (state != FINISHED) {
				switch (state) {
					case INIT: {
						//cat.debug("INIT"); 
						command = this.readCommand(dis); 
						if (command == COMMAND_REGISTER) {
							state = REGISTERING;
						} else {
							cat.warn("Invalid command for INIT:" + command); 
							state = FINISHED;
						}
						break;
					} case REGISTERING: {
						//cat.debug("REGISTERING"); 
						this.writeRegistrationResponse(dos);
						state = WAITING_ON_FUNCTION_REQUEST;
						break;
					} case WAITING_ON_FUNCTION_REQUEST: {
						command = this.readCommand(dis); 
						if (command == COMMAND_REQUEST_FUNCTION) {
							this.writeExperimentfunction(oos);
							state = WAITING_ON_EXPERIMENT_REQUEST;
						} else {
							cat.warn("Invalid command for WAITING_ON_FUNCTION_REQUEST:" + command); 
							state = FINISHED;
						}
						break;
					} case WAITING_ON_EXPERIMENT_REQUEST: {
						//cat.debug("WAITING_ON_EXPERIMENT_REQUEST"); 
						command = this.readCommand(dis); 
						if (command == COMMAND_REQUEST_EXPERIMENT) {
							this.resultAccepted = true;
							exp = this.server.createExperiment();
							this.writeExperimentResponse(exp, oos);
							state = WAITING_ON_EXPERIMENT_RESULT;
							// start timer
							monitor = new Timer();
							monitor.schedule(new MonitorTask(this, exp), 1000);
						} else {
							cat.warn("Invalid command for WAITING_ON_EXPERIMENT_REQUEST:" + command); 
							state = FINISHED;
						}
						break;
					} case WAITING_ON_EXPERIMENT_RESULT: {
						//cat.debug("WAITING_ON_EXPERIMENT_RESULT"); 
						exp = this.readResult(ois);
						// finish timer
						if (this.resultAccepted && exp != null) {
							monitor.cancel();
							this.server.finishedExperiment(exp);
							//cat.debug("Finshed exp:" + exp.id() + " client " + this.id); 
						} else {
							cat.error("Failed to finish experiment:" + this.resultAccepted); 
						}
						state = WAITING_ON_EXPERIMENT_REQUEST;
						break;
					}
					default: {
						break;
					}
				}
			}
		} catch (IOException e) {
			state = FINISHED;
			this.cancelExperiment(exp);
			e.printStackTrace();
		} catch (ExperimentNotRunException e) {
			state = FINISHED;
			this.cancelExperiment(exp);
			e.printStackTrace();
		} catch (Exception e) {
			state = FINISHED;
			this.cancelExperiment(exp);
			e.printStackTrace();
		} finally {
			try {
				ois.close();
				oos.close();
				dis.close();
				dos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}				
	}
	
	private void writeExperimentResponse(Experiment exp, ObjectOutputStream oos) {		
		//ObjectOutputStream oos = null;
		
		try {
			oos.writeObject(exp);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeRegistrationResponse(DataOutputStream dos) {	
		try {
			dos = new DataOutputStream(this.handlerConnection.getOutputStream());
			dos.write(MESSAGE_MARKER);
			dos.write(this.id);
			dos.write(MESSAGE_MARKER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void writeExperimentfunction(ObjectOutputStream oos) {
		try {
			oos.writeObject(this.server.function());
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Experiment readResult(ObjectInputStream ois) {
		Experiment exp = null;
		
		try {
			exp = (Experiment)ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return (exp);
	}	

	private int readCommand(DataInputStream dis) {
		int data;
		int command = COMMAND_INVALID;
		
		try {
			data = dis.read();
			if (data == MESSAGE_MARKER) {
				command = dis.read();
				data = dis.read();
				if (data != MESSAGE_MARKER) {
					command = COMMAND_INVALID;
				} 
			}
		} catch (IOException e) {
			command = COMMAND_INVALID;
		}
		
		return (command);
	}	
}
