/*
 *  Simbad - Robot Simulator
 *  Copyright (C) 2004 Louis Hugues
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 -----------------------------------------------------------------------------
 * $Author: sioulseuguh $ 
 * $Date: 2005/08/07 12:25:03 $
 * $Revision: 1.5 $
 * $Source: /cvsroot/simbad/src/simbad/sim/SimpleAgent.java,v $
 */
package simbad.sim;

import java.util.ArrayList;

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * This is the base class for all kinds of physical agents. <br> Implementation
 * note : the agent doesnt have synchronized methods. All thread refering to the
 * agent should do explicit synchronization with synchronized(agent){...}..
 */
public class SimpleAgent extends BaseObject {

    /**
     * Agent's printable name
     */
    protected String name = "";
    /**
     * Collision flag
     */
    boolean collisionDetected;
    /**
     * Interagent interaction indicator
     */
    boolean interactionDetected;
    /**
     * Keeps track of agent in physical contact with this agent. null most of
     * the time
     */
    SimpleAgent veryNearAgent;
    /**
     * Parent simulator.
     */
    private Simulator simulator;
    /**
     * The agent's sensors
     */
    private ArrayList sensors;
    /**
     * The agent's actuators
     */
    private ArrayList actuators;
    /**
     * for intermediate computations and to minimize Gargabe collection
     */
    protected Vector3d v1 = new Vector3d();
    protected Transform3D t3d1 = new Transform3D();
    protected Transform3D t3d2 = new Transform3D();
    protected Transform3D t3d3 = new Transform3D();
    /**
     * List of currently interacting agent
     */
    //private ArrayList interactingAgents;
    /**
     * Start and restart position of the agent
     */
    private Vector3d startPosition;
    private double startRotation;
    /**
     * Counter incremented on each simulation step
     */
    private int counter;
    /**
     * Lifetime in seconds since last reset
     */
    private double lifetime;
    protected double odometer;
    /**
     * Position has changed between two steps.
     */
    protected boolean positionChanged;
    protected Node body;
    // Physical parameters
    /**
     * Agent's Height in meters
     */
    protected float height;
    /**
     * Agent's radius in meters
     */
    protected float radius;
    /**
     * Agent's mass in kilogram
     */
    protected float mass;
    /**
     * Agent's static friction coefficient - 0 = no friction
     */
    protected float staticFrictionCoefficient;
    protected float dynamicFrictionCoefficient;
    /**
     * Current linear acceleration of the center of mass.
     */
    protected Vector3d linearAcceleration = new Vector3d();
    /**
     * Current angular acceleration about the center of mass.
     */
    protected Vector3d angularAcceleration = new Vector3d();
    /**
     * Current linear velocity of the center of mass.
     */
    protected Vector3d linearVelocity = new Vector3d();
    /**
     * Current angular velocity about the center of mass.
     */
    protected Vector3d angularVelocity = new Vector3d();
    /**
     * Current translation step.
     */
    protected Vector3d instantTranslation = new Vector3d();
    /**
     * Current rotation step.
     */
    protected Vector3d instantRotation = new Vector3d();
    /**
     * Used for collision picking
     */
    protected double collisionDistance[] = new double[1];
    protected double collisionRadius;

    /**
     * Constructs a SimpleAgent.
     *
     * @param pos the starting position.
     * @param name the name of the agent.
     */
    public SimpleAgent(Vector3d pos) {
        super.create3D(true);
        startPosition = new Vector3d(pos);
        startRotation = 0;
        sensors = new ArrayList();
        actuators = new ArrayList();
        // interactingAgents = new ArrayList();

        detachedFromSceneGraph = false;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setStartPosition(Vector3d pos) {
        this.startPosition = pos;
    }
    
    public Vector3d getStartPosition() {
        return this.startPosition;
    }

    public void setInitialRotation(double angle) {
        this.startRotation = angle;
    }

    protected void create3D() { /*
         * Overide
         */

    }

    /**
     * Creation phase - called once by the simulator.
     */
    protected void create() {
        create3D();
        // setup on the floor
        startPosition.add(new Vector3d(0, height / 2.0, 0));
    }

    /**
     * Resets agent variables and position
     */
    protected void reset() {
        veryNearAgent = null;
        collisionRadius = radius;
        counter = 0;
        lifetime = 0;
        odometer = 0;
        linearVelocity.set(0, 0, 0);
        resetPosition();
        resetDevices();
    }

    protected void resetPosition() {
        resetPositionAt(startPosition);
        super.rotateY(startRotation);
    }

    protected void resetPositionAt(Vector3d newPosition) {
        // reattach to graph if necessary
        if (detachedFromSceneGraph) {
            attach();
        }
        resetTransforms();
        collisionDetected = false;
        interactionDetected = false;
        translateTo(newPosition);
    }

    /**
     * Resets all devices
     */
    protected void resetDevices() {
        for (int i = 0; i < sensors.size(); i++) {
            SensorDevice sd = (SensorDevice) sensors.get(i);
            if (sd != null) {
                sd.reset();
            }

        }
        for (int i = 0; i < actuators.size(); i++) {
            ActuatorDevice ad = (ActuatorDevice) actuators.get(i);
            if (ad != null) {
                ad.reset();
            }
        }

    }

    /**
     * Update sensor phase - called on each simulation step.
     */
    protected void updateSensors(double elapsedSecond, BranchGroup pickableSceneBranch) {

        // don't want to sense its own body while picking (cause unnecessary intersections)
        body.setPickable(false);
        for (int i = 0; i < sensors.size(); i++) {
            SensorDevice sd = (SensorDevice) sensors.get(i);
            if (sd == null) {
                continue;
            }
            if (sd instanceof PickSensor) {
                ((PickSensor) sd).setPickableSceneBranch(pickableSceneBranch);
            }
            sd.update(elapsedSecond);
        }
        // we are pickable again.
        body.setPickable(true);
    }

    /**
     * Update actuators phase - called on each simulation step.
     */
    protected void updateActuators(double elapsedSecond) {
        for (int i = 0; i < actuators.size(); i++) {
            ActuatorDevice ad = (ActuatorDevice) actuators.get(i);
            if (ad == null) {
                continue;
            }
            ad.update(elapsedSecond);
        }
    }

    /**
     * set acceleration applied by motors .
     */
    protected void setMotorsAcceleration(double dt) {
        // no motor by default
        linearAcceleration.set(0, 0, 0);
        angularAcceleration.set(0, 0, 0);
    }

    ;
    

    
    /** Perform acceleration integration step .*/
    protected void integratesVelocities(double dt) {
        v1.set(linearAcceleration);
        v1.scale(dt);
        linearVelocity.add(v1);
        v1.set(angularAcceleration);
        v1.scale(dt);
        angularVelocity.add(v1);
    }

    /**
     * Perform velocities integration step .�
     */
    protected void integratesPositionChange(double dt) {
        instantTranslation.set(linearVelocity);
        instantTranslation.scale(dt);
        instantRotation.set(angularVelocity);
        instantRotation.scale(dt);
    }

    /**
     * returns the distance from agent base to ground .
     */
    protected double distanceToGround() {

        translation.get(v1);
        return (v1.y - this.height / 2);
    }

    /**
     * Update the agent's position with instantTranslation and instantRotation
     */
    protected void updatePosition() {
        Transform3D t3d = t3d1;

        //if (!collisionDetected) {
        // clip vertical deplacement to avoid traversing the floor
        translation.get(v1);
        double dist = v1.y - height / 2;
        if (instantTranslation.y < (-dist)) {
            instantTranslation.y = -dist;
        }
        double delta;
        // perform translation
        if (Double.isNaN(instantTranslation.x) || Double.isNaN(instantTranslation.y) || Double.isNaN(instantTranslation.z)
                || Double.isInfinite(instantTranslation.x) || Double.isInfinite(instantTranslation.y) || Double.isInfinite(instantTranslation.z)) {
            //System.out.println("Invalid translation: " + instantTranslation);
            instantTranslation.set(0, 0, 0);
        } else {
            t3d.setIdentity();
            t3d.setTranslation(instantTranslation);
            translation.mul(t3d);
            translationGroup.setTransform(translation);
        }

        //perform rotation
        t3d.setIdentity();
        t3d.rotY(instantRotation.y);
        rotation.mul(t3d1);
        try {
            rotationGroup.setTransform(rotation);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // add tranlation delta to odometer
        delta = instantTranslation.length();
        odometer += delta;
        positionChanged = (delta != 0);
        //}
    }

    /**
     * Update all counters on each step.
     */
    protected void updateCounters(double elapsedSecond) {
        counter++;
        lifetime += elapsedSecond;
    }

    /**
     * called by simulator init
     */
    protected void initPreBehavior() {
    }

    /**
     * called by simulator init
     */
    protected void initBehavior() {
    }

    /**
     * called by simulator loop
     */
    protected void performPreBehavior() {
    }

    /**
     * called by simulator loop
     */
    protected void performBehavior() {
    }

    /**
     * Returns the state of the geometric collision indicator.
     *
     * @return collision indicator.
     */
    public boolean collisionDetected() {
        return collisionDetected;
    }

    /**
     * Returns true if an interaction has been detected.
     *
     * @return interaction indicator.
     */
    public boolean interactionDetected() {
        return interactionDetected;
    }

    /**
     * Go to the start position of the agent.
     */
    public void moveToStartPosition() {

        resetPosition();
    }

    /**
     * Go to given position. Caution : set y coords to agent.height/2 you want
     * the agent to touch the floor.
     *
     * @param position - the new position.
     */
    public void moveToPosition(Vector3d position) {

        resetPositionAt(position);
    }

    /**
     * Go to given XZ position. Y coords is left unchanged.
     *
     * @param position - the new position.
     */
    public void moveToPosition(double x, double z) {

        Vector3d position = new Vector3d(x, startPosition.y, z);
        resetPositionAt(position);
    }

    /**
     * Adds a sensor device to the agent.
     *
     * @param num - the requested position in the sensor list.
     * @param sd - the device.
     * @param position - its position relative to agent's center.
     * @param angle - its angle in the XZ plane.
     * @return the num of the sensor
     */
    protected int addSensorDevice(SensorDevice sd, Vector3d position, double angle) {

        sensors.add(sd);
        sd.setOwner(this);
        sd.translateTo(position);
        sd.rotateY((float) angle);
        addChild(sd);
        return sensors.size() - 1;
    }

    /**
     * Adds a actuator device to the agent.
     *
     * @param num - the requested position in the sensor list.
     * @param sd - the device.
     * @param position - its position relative to agent's center.
     * @param angle - its angle in the XZ plane.
     * @return the num of the actuator
     */
    protected int addActuatorDevice(ActuatorDevice ad, Vector3d position, double angle) {

        actuators.add(ad);
        ad.setOwner(this);
        ad.translateTo(position);
        ad.rotateY((float) angle);
        addChild(ad);
        return actuators.size() - 1;
    }

    /**
     * Dispose all resources
     */
    protected void dispose() {
    }

    /**
     * Returns printable description of the agent. This may be multiline and
     * complex in subclasses.
     *
     * @return agent description as string.
     */
    public synchronized String asString() {
        return name;
    }

    /**
     * Sets the simulator in charge of this agent.
     */
    protected void setSimulator(Simulator simulator) {
        this.simulator = simulator;
    }

    public Simulator getSimulator() {
        return simulator;
    }

    //////////////////////////////////////////////////////////////////////
    /// Get methods.
    public ArrayList getSensorList() {
        return sensors;
    }

    public ArrayList getActuatorList() {
        return actuators;
    }

    /**
     * Returns the agent total lifetime since last reset (in seconds).
     *
     * @return lifetime in seconds.
     */
    public double getLifeTime() {
        return lifetime;
    }

    /**
     * Returns the number of behavior step per second, ie the nummber of time
     * the performBehavior is called per second
     *
     * @return an int
     */
    public int getFramesPerSecond() {
        return simulator.getFramesPerSecond();
    }

    /**
     * Return agents coordinates.
     *
     * @return agent point3d .
     */
    public void getCoords(Point3d coord) {
        Vector3d t = v1;
        translation.get(t);
        coord.set(t.x, t.y, t.z);
    }

    /**
     * Returns the agent counter. Counter is incrementented at each simulation
     * step.
     *
     * @return agent step counter.
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Returns the agent's name.
     *
     * @return agent's name .
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the agent's mass.
     */
    public float getMass() {
        return mass;

    }

    /**
     * Returns the agent's radius in meters.
     *
     * @return the agent radius in meters.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Returns the agent's height in meters.
     *
     * @return the agent height in meters.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Returns the sensor device designated by num. User will have to cast to
     * the appropriate class.
     *
     * @return a SensorDevice Object.
     */
    public SensorDevice getSensorDevice(int num) {
        return (SensorDevice) sensors.get(num);
    }

    /**
     * Returns the actuator device designated by num. User will have to cast to
     * the appropriate class.
     *
     * @return a ActuatorDevice Object.
     */
    public ActuatorDevice getActuatorDevice(int num) {
        return (ActuatorDevice) actuators.get(num);
    }

    //////////////////////////////////////////////////////////////////////
    /// Agent contact related methods
    /**
     * called back by simulator to clear physical interaction other agent.
     */
    protected void clearVeryNear() {
        veryNearAgent = null;
    }

    /**
     * called back by simulator when a physical interaction as occured with an
     * other agent.
     */
    protected void veryNear(SimpleAgent a) {
        veryNearAgent = a;
    }

    /**
     * Returns true if this agent is in physical contact with an other
     * SimpleAgent.
     */
    public boolean anOtherAgentIsVeryNear() {
        return (veryNearAgent != null);
    }

    /**
     * Returns the currently touched agent - null if no agent near.
     */
    public SimpleAgent getVeryNearAgent() {
        return veryNearAgent;
    }
}
