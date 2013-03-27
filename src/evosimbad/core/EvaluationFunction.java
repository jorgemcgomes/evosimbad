/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.io.Serializable;

/**
 *
 * @author Jorge
 */
public abstract class EvaluationFunction implements Serializable {
    
    protected transient Simulation experiment;
    private int totalEvaluations;
    private int currentEvaluation = 0;
    private int updateInterval;
    private int totalSteps;
    
    public EvaluationFunction() {
        this(1);
    }
    
    public EvaluationFunction(int updateInterval) {
        this.updateInterval = updateInterval;
    }
    
    public void setSimulatorExperiment(Simulation exp) {
        this.experiment = exp;
        this.totalEvaluations = experiment.getTotalSteps() / updateInterval;
        this.totalSteps = experiment.getTotalSteps();
    }
    
    public int getTotalEvaluations() {
        return totalEvaluations;
    }
    
    public int getCurrentEvaluation() {
        return currentEvaluation;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public int getTotalSteps() {
        return totalSteps;
    }
    
    public void step() {
        if(experiment.getCurrentStep() % updateInterval == 0) {
            currentEvaluation++;
            evaluate();
        }
    }
    
    public abstract void evaluate();
    
    public abstract double getFitness();
    
}
