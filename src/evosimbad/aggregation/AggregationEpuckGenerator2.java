/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation;

import evosimbad.commons.VariableAgentGenerator;
import evosimbad.core.NNAgent;

/**
 *
 * @author Jorge
 */
public class AggregationEpuckGenerator2 extends VariableAgentGenerator {

    private float robotDistance;
    private boolean highlight;

    public AggregationEpuckGenerator2(int min, int max, float robotDistance, boolean highlight) {
        super(min, max);
        this.robotDistance = robotDistance;
        this.highlight = highlight;
    }

    @Override
    protected NNAgent generateAgent() {
        AggregationEpuck ae = new AggregationEpuck(highlight);
        ae.setObstacleIR(0.1f, 8);
        ae.setRobotIR(robotDistance, 16);
        ae.setNearCount(robotDistance + ae.getRadius() * 2, -1);
        ae.toggleStopActuator(true);
        return ae;
    }

    @Override
    public int getInputs() {
        return 8 + 8 + 1;
    }

    @Override
    public int getOutputs() {
        return 3;
    }
}
