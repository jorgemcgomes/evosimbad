/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import evosimbad.commons.SquareWithObstacles.Obstacle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;
import org.apache.commons.lang3.tuple.Pair;
import simbad.sim.EnvironmentDescription;
import simbad.sim.StaticObject;

/**
 *
 * @author Jorge
 */
public class ObstaclesRandomPlacer extends RandomAgentPlacer {
   
    private transient Bounds[] obstacleBounds;
    
    public ObstaclesRandomPlacer(float safeMargin, float separation) {
        super(safeMargin, separation);
    }

    @Override
    public List<Pair<Point3d, Double>> generateStartPositions(EnvironmentDescription ed, int count) {
        ArrayList<Bounds> bs = new ArrayList<>();
        for(StaticObject obj : ed.getStaticObjects()) {
            if(obj instanceof Obstacle) {
                Obstacle obs = (Obstacle) obj;
                bs.add(obs.getRealBounds());
                System.out.print(obs.getRealBounds());
            }
        }        
        obstacleBounds = new Bounds[bs.size()];
        obstacleBounds = bs.toArray(obstacleBounds);
        return super.generateStartPositions(ed, count);
    }
    
    @Override
    protected boolean isValid(LinkedList<Point3d> placed, Point3d candidate, EnvironmentDescription ed) {
        if(!super.isValid(placed, candidate, ed)) {
            return false;
        }
        // Check separation from obstacles
        Bounds agentBounds = new BoundingSphere(candidate, safeMargin);
        return !agentBounds.intersect(obstacleBounds);
    }
    
}
