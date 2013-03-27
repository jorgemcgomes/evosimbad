/* author: Louis Hugues - created on 12 f�vr. 2005  */
package simbad.gui;

import java.awt.Frame;
import java.awt.Panel;
import javax.media.j3d.Canvas3D;
import simbad.sim.EnvironmentDescription;
import simbad.sim.Simulator;
import simbad.sim.World;

/**
 * Runs simbad simulator in batch mode with no user interface (only small 3d window).
 * Using the folling scenario:
 * construct->reset->step, step ,..., step -> dispose-> System.exit
 */
public class Simbatch {

    public int counter;
    public World world;
    public Simulator simulator;
    public Canvas3D canvas3d;
    public Panel panel;

    /** Construct a batch version of Simbad simulator */
    public Simbatch(EnvironmentDescription ed) {
        // see Bug ID: 4727054 
        counter = 0;
        world = new World(ed, true);            
        simulator = new Simulator(null, world, ed);
        world.dispose();
    }
    
    public void changeEnvironment(EnvironmentDescription ed) {
        simulator.changeEnvironment(ed);
    }

    /** Restart the simulation */
    public void reset() {
        simulator.resetSimulation();
        simulator.initBehaviors();
    }

    /** perform one step - call it in your main loop*/
    public void step() {
        simulator.simulateOneStep();
    }

    /** Dispose resource at end.**/
    public void dispose() {
        simulator.dispose();
        world.dispose();
        simulator = null;
        world = null;
        canvas3d = null;
        //System.runFinalization();
        //System.gc();
    }
}
