/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 *
 * @author Jorge
 */
public abstract class SimulationBuilder implements Serializable {

    protected AgentGenerator agentGenerator;
    protected AgentPlacer agentPlacer;
    protected EnvironmentGenerator envGenerator;
    protected EvaluationFunction functionSample;
    private Class evaluationClass;
    private Class[] evaluationConstructor;
    private Object[] evaluationArgs;

    void setAgentGenerator(AgentGenerator agentGenerator) {
        this.agentGenerator = agentGenerator;
    }

    void setAgentPlacer(AgentPlacer agentPlacer) {
        this.agentPlacer = agentPlacer;
    }

    void setEnvGenerator(EnvironmentGenerator envGenerator) {
        this.envGenerator = envGenerator;
    }

    void setEvaluationFunction(Class evalClass, Class[] evalConstructor, Object[] evalArgs) {
        this.evaluationClass = evalClass;
        this.evaluationConstructor = evalConstructor;
        this.evaluationArgs = evalArgs;
        this.functionSample = createEvaluationFunction();
    }

    protected EvaluationFunction createEvaluationFunction() {
        try {
            Constructor constructor = evaluationClass.getConstructor(evaluationConstructor);
            return (EvaluationFunction) constructor.newInstance(evaluationArgs);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public abstract List<Simulation> prepareExperiments(NeuralNet controller);
}