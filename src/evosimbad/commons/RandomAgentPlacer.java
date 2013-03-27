/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import evosimbad.core.AgentPlacer;
import java.util.LinkedList;
import java.util.List;
import javax.vecmath.Point3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import simbad.sim.Agent;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author Jorge
 */
public class RandomAgentPlacer implements AgentPlacer {

    protected float safeMargin, separation;
    
    public RandomAgentPlacer(float safeMargin, float separation) {
        this.safeMargin = safeMargin;
        this.separation = separation;
    }
    
    protected boolean isValid(LinkedList<Point3d> placed, Point3d candidate, EnvironmentDescription ed) {
        for(Point3d p : placed) {
            if(p.distance(candidate) < separation) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Pair<Point3d, Double>> generateStartPositions(EnvironmentDescription ed, int count) {
        LinkedList<Pair<Point3d, Double>> positions = new LinkedList<>();
        LinkedList<Point3d> placed = new LinkedList<>();
        
        // if there are already agents placed
        for(Agent a : ed.getAgents()) {
            placed.add(new Point3d(a.getStartPosition()));
        }
        
        float bound = ed.worldSize / 2 - safeMargin;
        while(positions.size() < count) {
            float x = (float) ((Math.random() * 2 - 1) * bound);
            float z = (float) ((Math.random() * 2 - 1) * bound);
            Point3d candidate = new Point3d(x, 0, z);
            if(isValid(placed, candidate, ed)) {
                double rot = Math.random() * Math.PI * 2;
                positions.add(new ImmutablePair(candidate, rot));
                placed.add(candidate);
            }
        }
        return positions;
    }
}
