/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.dispersion;

import evosimbad.commons.Epuck;
import evosimbad.commons.VariableAgentGenerator;
import evosimbad.core.NNAgent;

/**
 *
 * @author jorge
 */
public class DispersionAgentGenerator extends VariableAgentGenerator {

    private float robotRange;

    public DispersionAgentGenerator(int min, int max, float robotRange) {
        super(min, max);
        this.robotRange = robotRange;
    }

    @Override
    protected NNAgent generateAgent() {
        Epuck ag = new Epuck(5);
        ag.setObstacleIR(0.1f, 8);
        ag.setRobotIR(robotRange, 24);
        ag.toggleStopActuator(true);
        return ag;
    }

    @Override
    public int getInputs() {
        return 16;
    }

    @Override
    public int getOutputs() {
        return 3;
    }
}
