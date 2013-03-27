/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import evosimbad.core.EnvironmentGenerator;
import javax.vecmath.Vector3d;
import simbad.sim.EnvironmentDescription;
import simbad.sim.Wall;

/**
 *
 * @author Jorge
 */
public class SquareGenerator implements EnvironmentGenerator {
    
    private float worldSize;
    
    public SquareGenerator(float worldSize) {
        this.worldSize = worldSize;
    }

    @Override
    public EnvironmentDescription generateEnvironment() {
        EnvironmentDescription ed = new EnvironmentDescription();
        ed.worldSize = worldSize;
        ed.setUsePhysics(true);
        Wall w1 = new Wall(new Vector3d(worldSize/2, 0, 0), worldSize, 0.2f, ed);
        w1.rotate90(1);
        ed.add(w1);
        Wall w2 = new Wall(new Vector3d(-worldSize/2, 0, 0), worldSize, 0.2f, ed);
        w2.rotate90(1);
        ed.add(w2);
        Wall w3 = new Wall(new Vector3d(0, 0, worldSize/2), worldSize, 0.2f, ed);
        ed.add(w3);
        Wall w4 = new Wall(new Vector3d(0, 0, -worldSize/2), worldSize, 0.2f, ed);
        ed.add(w4);
        return ed;        
    }
    
}
