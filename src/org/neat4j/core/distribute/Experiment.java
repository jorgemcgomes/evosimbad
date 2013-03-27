package org.neat4j.core.distribute;

import java.io.Serializable;

import org.neat4j.neat.core.NEATFitnessFunction;
import org.neat4j.neat.ga.core.Chromosome;

public class Experiment implements Serializable {
	private static final long serialVersionUID = 1L;
	private NEATFitnessFunction fitnessFunction;
	private Chromosome genoType;
	private int id;
	private boolean hasRun = false;
	
	/**
	 * Creates a named experiment
	 * @param id
	 */
	public Experiment(int id) {
		this.id = id;
	}

	/**
	 * Sets up the Experiment execution environment 
	 * @param fitnessFunction
	 */
	public void setupFunction(NEATFitnessFunction fitnessFunction) {		
		this.fitnessFunction = fitnessFunction;
	}

	/**
	 * Assigns the individual chromosome to run the experiment
	 * @param genoType
	 */
	public void setupExperiment(Chromosome genoType) {		
		this.genoType = genoType;
	}
	
	/**
	 * runs the experiment
	 *
	 */
	public void performExperiment() {
		double eval = this.fitnessFunction.evaluate(this.genoType);
		this.genoType.updateFitness(eval);
		this.hasRun = true;
	}
	
	/**
	 * Returns the experiment result if it has been set up correctly and run
	 * @return Fitness updated chromosome
	 * @throws ExperimentNotRunException if the experiments has not yet been run.
	 */
	public Chromosome result() throws ExperimentNotRunException {
		if (!this.hasRun) {
			throw new ExperimentNotRunException("result() called before experiment has run");
		}
		return (this.genoType);
	}
	
	/**
	 * Experiment number
	 * @return
	 */
	public int id() {
		return (this.id);
	}
}
