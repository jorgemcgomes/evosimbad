/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.escape;

import evosimbad.core.EnvironmentGenerator;
import java.awt.Color;
import java.util.HashSet;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import simbad.sim.Agent;
import simbad.sim.Box;
import simbad.sim.EnvironmentDescription;
import simbad.sim.Wall;

/**
 *
 * @author jorge
 */
public class EscapeEnvironment implements EnvironmentGenerator {

    private float size, doorSize, escapeWindow;

    public EscapeEnvironment(float size, float doorSize, float escapeWindow) {
        this.size = size;
        this.doorSize = doorSize;
        this.escapeWindow = escapeWindow;
    }

    @Override
    public EnvironmentDescription generateEnvironment() {
        return new EscapeEnv();
    }

    public class EscapeEnv extends EnvironmentDescription {

        private HashSet<EscapeAgentGenerator.EscapeAgent> fugitives = new HashSet<>();
        private double started = 0;
        private double elapsedTime = 0;
        private Door door;
        private Point3d doorLocation;

        public EscapeEnv() {
            this.worldSize = size;
            super.setUsePhysics(true);
            Wall w1 = new Wall(new Vector3d(worldSize / 2, 0, 0), worldSize, 0.2f, this);
            w1.rotate90(1);
            super.add(w1);
            Wall w2 = new Wall(new Vector3d(-worldSize / 2, 0, 0), worldSize, 0.2f, this);
            w2.rotate90(1);
            super.add(w2);
            Wall w4 = new Wall(new Vector3d(0, 0, -worldSize / 2), worldSize, 0.2f, this);
            super.add(w4);

            double pos = (worldSize / 2 + doorSize / 2) / 2;
            double len = (worldSize - doorSize) / 2;
            Wall w3L = new Wall(new Vector3d(pos, 0, worldSize / 2), (float) len, 0.2f, this);
            super.add(w3L);
            Wall w3R = new Wall(new Vector3d(-pos, 0, worldSize / 2), (float) len, 0.2f, this);
            super.add(w3R);
            doorLocation = new Point3d(0, 0, worldSize / 2);
            door = new Door(new Vector3d(doorLocation), doorSize, 0.2f, this);
            super.add(door);
        }

        @Override
        public void updateEnvironment(double dt) {
            elapsedTime += dt;
            Point3d pos = new Point3d();
            float bounds = worldSize / 2;
            int t = 1;
            for (Agent ag : getAgents()) {
                EscapeAgentGenerator.EscapeAgent ea = (EscapeAgentGenerator.EscapeAgent) ag;
                ea.getCoords(pos);
                if (!fugitives.contains(ea) && (pos.x > bounds || pos.x < -bounds || pos.z > bounds || pos.z < -bounds)) { // fugiu mais um
                    fugitives.add(ea);
                    ea.setStatus(EscapeAgentGenerator.EscapeAgent.ESCAPED);
                    ea.moveToPosition(1000 * t, 1000 * t);
                    if (started == 0) {
                        started = elapsedTime;
                    }
                }
                t++;
            }

            if ((started > 0 && elapsedTime > started + escapeWindow)
                    || fugitives.size() == getAgents().size()) { // acabou a fuga
                for (Agent ag : getAgents()) {
                    EscapeAgentGenerator.EscapeAgent ea = (EscapeAgentGenerator.EscapeAgent) ag;
                    if (!fugitives.contains(ea)) {
                        ea.setStatus(EscapeAgentGenerator.EscapeAgent.INSIDE);
                    }
                }
                door.close();
            }
        }

        public Point3d getDoorLocation() {
            return doorLocation;
        }

        public Door getDoor() {
            return door;
        }

        public float getEscapeWindow() {
            return escapeWindow;
        }
    }

    public static class Door extends Box {

        private boolean closed = false;

        public Door(Vector3d pos, float length, float height, EnvironmentDescription wd) {
            super(pos, new Vector3f(length, height, 0.02f), wd);
            super.setColor(new Color3f(Color.WHITE));
            super.setCanBeTraversed(true);
        }

        public void close() {
            closed = true;
            super.setCanBeTraversed(false);
            super.material.setAmbientColor(new Color3f(Color.RED));
            super.material.setDiffuseColor(new Color3f(Color.RED));
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
