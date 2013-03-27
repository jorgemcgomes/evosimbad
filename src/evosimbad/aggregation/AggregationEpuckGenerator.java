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
public class AggregationEpuckGenerator extends VariableAgentGenerator {

    private float robotDistance;
    private boolean highlight;
    private boolean useCount;

    public AggregationEpuckGenerator(int min, int max, float robotDistance, boolean useCount, boolean highlight) {
        super(min, max);
        this.robotDistance = robotDistance;
        this.highlight = highlight;
        this.useCount = useCount;
    }

    @Override
    protected NNAgent generateAgent() {
        AggregationEpuck ae = new AggregationEpuck(highlight);
        ae.setObstacleIR(0.1f, 8);
        ae.setRobotIR(robotDistance, 24);
        if(useCount) {
            ae.setNearCount(robotDistance + ae.getRadius() * 2, -1);
        }
        return ae;
    }

    @Override
    public int getInputs() {
        return useCount ? 8 + 8 + 1 : 8 + 8;
    }

    @Override
    public int getOutputs() {
        return 2;
    }
}
