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
public class StateCountTester extends EvaluationFunction {

    private HashMap<Integer, Float> map = new HashMap<>();
    private HashMap<Integer, byte[]> patternKeys = new HashMap<>();
    private int bins;
    private EvaluationFunction fitnessFunction;

    public StateCountTester(int updateRate, int bins, String fit) {
        super(updateRate);
        this.bins = bins;
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
        for (Agent ag : agents) {
            EpuckBase robot = (EpuckBase) ag;
            // normalizados entre [-1,1]
            double[] sensors = robot.readSensors();
            // normalizar para [-1,1]
            double left = EpuckBase.normalize(robot.getKinematics().getLeftVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
            double right = EpuckBase.normalize(robot.getKinematics().getRightVelocity(), -EpuckBase.MAX_WHEEL_VELOCITY, EpuckBase.MAX_WHEEL_VELOCITY);
            double[] vec = new double[sensors.length + 2];
            System.arraycopy(sensors, 0, vec, 0, sensors.length);
            vec[vec.length - 2] = left;
            vec[vec.length - 1] = right;
            byte[] disc = discretise(vec);
            int hash = hashVector(disc);
            if (!map.containsKey(hash)) {
                map.put(hash, (float) 1.0 / agents.size());
                patternKeys.put(hash, disc);
            } else {
                map.put(hash, map.get(hash) + (float) 1.0 / agents.size());
            }
        }
    }

    public Map<Integer, Float> getMap() {
        return map;
    }

    public Map<Integer, byte[]> getKey() {
        return patternKeys;
    }

    // from [-1,1] to [0..bins-1]
    private byte[] discretise(double[] real) {
        byte[] res = new byte[real.length];
        for (int i = 0; i < real.length; i++) {
            // scale de [-1,1] para [0,2] para [0,1] para [0,bins-1] e depois arredondar   
            res[i] = (byte) Math.round((real[i] + 1) / 2 * (bins - 1));
        }
        return res;
    }

    private int hashVector(byte[] array) {
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
}
