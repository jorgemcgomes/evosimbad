/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.collaboration;

import evosimbad.commons.VariableAgentGenerator;
import evosimbad.core.NNAgent;

/**
 *
 * @author jorge
 */
public class CollaborativeEpuckGenerator2 extends VariableAgentGenerator {


    public CollaborativeEpuckGenerator2(int min, int max) {
        super(min, max);
    }

    @Override
    protected NNAgent generateAgent() {
        CollaborativeEpuck ep = new CollaborativeEpuck();
        ep.setObstacleIR(0.10f, 8);
        ep.setRobotIR(0.25f, 24);
        ep.setNearCount(0.25f, -1);
        ep.setOverTaskSensor();
        ep.toggleStopActuator(true);
        return ep;
    }

    @Override
    public int getInputs() {
        return 8 + 8 + 1 + 1;
    }

    @Override
    public int getOutputs() {
        return 3;
    }
}
