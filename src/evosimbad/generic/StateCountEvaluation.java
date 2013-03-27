/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.commons.EpuckBase;
import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.core.Simulation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.Pair;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class StateCountEvaluation extends EvaluationFunction {

    protected EvaluationFunction fitnessFunction;
    protected int bins;
    protected boolean savePatterns;
    protected HashMap<Integer, Float> countMap = new HashMap<>(200);
    protected HashMap<Integer, byte[]> patternMap;
    private StateCount stateCount;

    public StateCountEvaluation(String fit, int updateInterval, int bins, boolean savePatterns) {
        super(updateInterval);
        this.bins = bins;
        this.savePatterns = savePatterns;
        if (savePatterns) {
            patternMap = new HashMap<>(200);
        }
        try {
            Pair<Constructor, Object[]> fitConstructor = ComponentLoader.findConstructor(EvaluationFunction.class, fit);
            fitnessFunction = (EvaluationFunction) fitConstructor.getLeft().newInstance(fitConstructor.getRight());
        } catch (Exception ex) {
            Logger.getLogger(StateCountTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
    public void evaluate() {
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
        int swarmSize = 0;
        for (Agent ag : agents) {
            if (shouldCount(ag)) {
                swarmSize++;
            }
        }

        for (Agent ag : agents) {
            if (shouldCount(ag)) {
                double[] data = readData(ag);
                byte[] disc = discretise(data);
                int hash = hashVector(disc);
                if (!countMap.containsKey(hash)) {
                    countMap.put(hash, 1f / swarmSize);
                    if (savePatterns) {
                        patternMap.put(hash, disc);
                    }
                } else {
                    countMap.put(hash, countMap.get(hash) + 1f / swarmSize);
                }
            }
        }
    }

    protected boolean shouldCount(Agent ag) {
        return ag instanceof EpuckBase;
    }

    protected double[] readData(Agent ag) {
        EpuckBase robot = (EpuckBase) ag;
        double[] sensors = robot.readSensors();
        double left = EpuckBase.normalize(robot.getKinematics().getLeftVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        double right = EpuckBase.normalize(robot.getKinematics().getRightVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
        double[] vec = new double[sensors.length + 2];
        System.arraycopy(sensors, 0, vec, 0, sensors.length);
        vec[vec.length - 2] = left;
        vec[vec.length - 1] = right;
        return vec;
    }
    
    protected int[] getComposition() {
        int size = patternMap.values().iterator().next().length;
        int[] comp = new int[size];
        Arrays.fill(comp, 1);
        return comp;
    }

    protected byte[] discretise(double[] real) {
        byte[] res = new byte[real.length];
        for (int i = 0; i < real.length; i++) {
            // scale de [-1,1] para [0,2] para [0,1] para [0,bins-1] e depois arredondar   
            res[i] = (byte) Math.round((real[i] + 1) / 2 * (bins - 1));
        }
        return res;
    }

    // 
    protected int hashVector(byte[] array) {
        int hash = 0;
        for (byte b : array) {
            hash += (b & 0xFF);
            hash += (hash << 10);
            hash ^= (hash >>> 6);
        }
        hash += (hash << 3);
        hash ^= (hash >>> 11);
        hash += (hash << 15);
        return hash;
    }

    @Override
    public double getFitness() {
        return fitnessFunction.getFitness();
    }

    public StateCount getStateCount() {
        if (stateCount == null) {
            stateCount = new StateCount(countMap, patternMap);
        }
        return stateCount;
    }

    public int getBins() {
        return bins;
    }
    
    
}
