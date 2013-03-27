/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.escape;

import evosimbad.escape.EscapeAgentGenerator.EscapeAgent;
import evosimbad.generic.EpuckStateCount;
import org.apache.commons.lang3.ArrayUtils;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class EscapeStateCount extends EpuckStateCount {

    public EscapeStateCount(String fit, int updateInterval, int bins, boolean savePatterns) {
        super(fit, updateInterval, bins, savePatterns);
    }

    @Override
    protected double[] readData(Agent ag) {
        EscapeAgent ea = (EscapeAgent) ag;
        if (ea.getStatus() == EscapeAgent.RUNNING) {
            double[] data = super.readData(ag);
            double[] doorSensor = reduce8to4(ea.readDoorSensor());
            double[] res = ArrayUtils.addAll(data, doorSensor);
            res = ArrayUtils.add(res, 0);
            return res;
        } else if(ea.getStatus() == EscapeAgent.INSIDE) {
            return new double[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0};
        } else {
            return new double[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        }
    }
}
