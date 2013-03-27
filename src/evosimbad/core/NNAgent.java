/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import javax.vecmath.Vector3d;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.csv.CSVInput;
import org.neat4j.neat.nn.core.NeuralNet;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public abstract class NNAgent extends Agent {

    protected NeuralNet controller;

    public NNAgent() {
        super(new Vector3d(0, 0, 0));
    }

    public void setController(NeuralNet controller) {
        this.controller = controller;
    }

    @Override
    protected void performBehavior() {
        double[] input = readSensors();
        NetworkInput in = new CSVInput(input);
        NetworkOutputSet output = controller.execute(in);
        double[] out = output.nextOutput().values();
        performBehavior(out);
    }

    /**
     * @param output The vales are as outputed by the neural network
     */
    protected abstract void performBehavior(double[] output);

    /**
     * Assumes the values are normalized, ready to be given to the neural network
     * @return 
     */
    public abstract double[] readSensors();

    /*@Override
     public void reset() {
     super.reset();
     if(controller instanceof NeuralNet) {
     ((NeuralNet) controller).clearContext();
     }
     }*/
    /**
     * scales [min, max] to [-1, 1]
     */
    public static double normalize(double val, double min, double max) {
        return ((val - min) / (max - min)) * 2 - 1;
    }

    /**
     * scales [0, 1] to [min, max]
     */
    public static double denormalize(double val, double min, double max) {
        return val * (max - min) + min;
    }
}
