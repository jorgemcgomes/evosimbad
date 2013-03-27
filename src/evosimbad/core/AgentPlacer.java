/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.io.Serializable;
import java.util.List;
import javax.vecmath.Point3d;
import org.apache.commons.lang3.tuple.Pair;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author Jorge
 */
public interface AgentPlacer extends Serializable {
    
    public List<Pair<Point3d, Double>> generateStartPositions(EnvironmentDescription ed, int count);
    
}
