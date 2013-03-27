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
public class CollaborativeEpuckGenerator extends VariableAgentGenerator {

    private float range;

    public CollaborativeEpuckGenerator(int min, int max, float range) {
        super(min, max);
        this.range = range;
    }

    @Override
    protected NNAgent generateAgent() {
        CollaborativeEpuck ep = new CollaborativeEpuck();
        if(range > 0) {
            ep.setTaskSensor(range, 16);
        }
        ep.setObstacleIR(0.10f, 8);
        ep.setRobotIR(0.25f, 24);
        ep.setNearCount(0.25f, -1);
        ep.setOverTaskSensor();
        ep.toggleStopActuator(true);
        return ep;
    }

    @Override
    public int getInputs() {
        return range > 0 ? 26 : 18;
    }

    @Override
    public int getOutputs() {
        return 3;
    }
}
