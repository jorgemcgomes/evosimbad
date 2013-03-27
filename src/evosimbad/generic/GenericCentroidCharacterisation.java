/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.commons.EpuckBase;
import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.core.Simulation;
import evosimbad.evolution.InformedCharacterisation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.Pair;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public abstract class GenericCentroidCharacterisation extends InformedCharacterisation {

    protected EvaluationFunction fitnessFunction;
    protected int timeSteps;
    protected transient ArrayList<double[]> behaviours;
    private double[] finalBehaviour;
    private int dataLength = -1;
    protected int count = 0;
    private int sizeAux = 0;

    public GenericCentroidCharacterisation(String fit, int updateRate, int timeSteps) {
        super(updateRate);
        this.timeSteps = timeSteps;
        this.behaviours = new ArrayList<>(timeSteps);
        try {
            Pair<Constructor, Object[]> fitConstructor = ComponentLoader.findConstructor(EvaluationFunction.class, fit);
            fitnessFunction = (EvaluationFunction) fitConstructor.getLeft().newInstance(fitConstructor.getRight());
        } catch (Exception ex) {
            Logger.getLogger(StateCountTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Fitness related methods
     */
    @Override
    public void setSimulatorExperiment(Simulation exp) {
        super.setSimulatorExperiment(exp);
        fitnessFunction.setSimulatorExperiment(exp);
    }

    @Override
    public void step() {
        super.step();
        fitnessFunction.step();
    }

    @Override
    public double getFitness() {
        return fitnessFunction.getFitness();
    }

    /*
     * Novelty methods
     */
    @Override
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();

        // initialization procedure
        if (dataLength == -1) {
            for (Agent ag : agents) {
                if (shouldCount(ag)) {
                    dataLength = readData(ag).length;
                }
            }
        }

        // change timestep

        if ((getCurrentEvaluation() - 1) % (getTotalEvaluations() / timeSteps) == 0) {
            // normalize last one
            if (behaviours.size() > 0) {
                double[] b = behaviours.get(behaviours.size() - 1);
                for (int i = 0; i < b.length; i++) {
                    b[i] = b[i] / sizeAux;
                }
            }

            // create new one
            sizeAux = 0;
            double[] newElement = new double[dataLength];
            Arrays.fill(newElement, 0);
            behaviours.add(newElement);
        }

        // count the number of agents
        int total = 0;
        for (Agent ag : agents) {
            if (shouldCount(ag)) {
                total++;
            }
        }

        // compute the average behaviour
        for (Agent ag : agents) {
            if (shouldCount(ag)) {
                double[] data = readData(ag);
                //byte[] disc = discretise(data);
                double[] b = behaviours.get(behaviours.size() - 1);
                for (int i = 0; i < data.length; i++) {
                    b[i] += data[i] / total;
                }
                count++;
            }
        }
        sizeAux++;

        // termination procedures
        if (getCurrentEvaluation() == getTotalEvaluations()) {
            // normalize last one
            double[] b = behaviours.get(behaviours.size() - 1);
            for (int i = 0; i < b.length; i++) {
                b[i] = b[i] / sizeAux;
            }

            // aggregate behaviours
            finalBehaviour = new double[dataLength * behaviours.size()];
            for (int i = 0; i < behaviours.size(); i++) {
                System.arraycopy(behaviours.get(i), 0, finalBehaviour, i * dataLength, dataLength);
            }
        }
    }
    
    @Override
    public double[] getBehaviour() {
        return finalBehaviour;
    }

    protected boolean shouldCount(Agent ag) {
        return ag instanceof EpuckBase;
    }

    /**
     * Each element must be in the range [-1,1]
     * @param ag
     * @return 
     */
    public abstract double[] readData(Agent ag);
}
