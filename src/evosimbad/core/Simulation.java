/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import simbad.sim.EnvironmentDescription;

/**
 *
 * @author Jorge
 */
public class Simulation {

    private EnvironmentDescription environment;
    private EvaluationFunction evaluationFunction;
    private int totalSteps;
    private int fps;
    private int currentStep;

    public Simulation(EnvironmentDescription ed, int steps, int fps, EvaluationFunction ef) {
        this.totalSteps = steps;
        this.fps = fps;
        this.environment = ed;
        this.currentStep = 0;        
        this.evaluationFunction = ef;
        ef.setSimulatorExperiment(this);
    }

    public void step() {
        evaluationFunction.step();
        currentStep++;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public EnvironmentDescription getEnvironment() {
        return environment;
    }

    public EvaluationFunction getEvaluationFunction() {
        return evaluationFunction;
    }

    public int getTotalSteps() {
        return totalSteps;
    }
    
    public int getFPS() {
        return fps;
    }
    
    public void done() {
        environment = null;
    }
}
