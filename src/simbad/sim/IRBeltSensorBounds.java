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
 * $Date: 2005/03/17 17:49:37 $
 * $Revision: 1.13 $
 * $Source: /cvsroot/simbad/src/simbad/sim/RangeSensorBelt.java,v $
 * 21/02/2005 measurrment init value is Double.POSITIVE_INFINITY.
 * 
 * History:
 * Modif. LH 01-oct-2006 : correct getLeftQuadrantMeasurement.
 */
package simbad.sim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.media.j3d.Appearance;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * This class models a circular belt of range sensors : Sonar , Bumpers (future
 * : Laser, Ir). Sensors are arranged circularly around the robot. User can
 * access to the measurement and hits state of each sensor individualy or obtain
 * an average measurement in a quadrant. <br><br> Note that the sensors are not
 * affected by noise. however you can easily add some gaussian noise with the
 * java.util.Random class. <br><code> Random generator = new Random(seed);
 * </code> <br>and<br>
 * <code> value = sonars.getMeasurement(0)+generator.nextGaussian()*stddev;</code>
 * <br> <br><br> Implementation notes : We use java 3D picking feature to
 * emulate sensing. A PickCylinderRay is used on each update to test whether
 * there is an potential obstacle. Each ray is then checked with a PickSegement.
 */
public class IRBeltSensorBounds extends PickSensor {

    private Class objectType;
    private float maxRange;
    private float distanceSteps;
    private float radius;
    private int nbSensors;
    /**
     * for storing results
     */
    private double measurements[];
    private boolean hits[];
    private boolean oneHasHit;
    /**
     * angular position (deduced from positions infos)
     */
    private double angles[];
    /**
     * * position of each sensor relative to center
     */
    private Vector3d positions[];
    /**
     * direction vector of each sensor - relative to sensor position.
     */
    private Vector3d directions[];
    private Transform3D t3d;
    //  private Transform3D t3d_2;
    private Point3d start;
    private Color3f color;
    /**
     * for picking
     */
    private boolean showRay;
    private Simulator simulator; // needed for acessing the objects

    /**
     * Constructs a RangeSensorBelt. The sensor type can be either
     * TYPE_BUMPER,TYPE_SONAR,TYPE_IR or TYPE_LASER. Ranges are measured from
     * the belt perimeter (not from the belt center).
     *
     * @param radius - the radius of the belt.
     * @param minRange - the minimal range of each sensor ray. Not used for
     * TYPE_BUMPER.
     * @param maxRange - the maximal range of each sensor ray. Not used for
     * TYPE_BUMPER.
     * @param nbsensors - the number of sensors in the belt (typically 4,6,12,24
     * or 36).
     * @param type - to specify the sensor behavior
     */
    public IRBeltSensorBounds(float radius, float maxRange, float distanceSteps,
            int nbsensors, Class objectType, boolean showRay) {
        // compute angles ,positions , directions
        positions = new Vector3d[nbsensors];
        directions = new Vector3d[nbsensors];
        Vector3d frontPos = new Vector3d(radius, 0, 0);
        Vector3d frontDir = new Vector3d(maxRange, 0, 0);
        angles = new double[nbsensors];
        Transform3D transform = new Transform3D();
        for (int i = 0; i < nbsensors; i++) {
            angles[i] = i * 2 * Math.PI / (double) nbsensors;
            transform.setIdentity();
            transform.rotY(angles[i]);
            Vector3d pos = new Vector3d(frontPos);
            transform.transform(pos);
            positions[i] = pos;
            Vector3d dir = new Vector3d(frontDir);
            transform.transform(dir);
            directions[i] = dir;
        }
        this.distanceSteps = distanceSteps;
        initialize(radius, maxRange, nbsensors, objectType, showRay);
    }

    /**
     * Constructs a RangeSensorBelt. The sensor type can be either
     * TYPE_BUMPER,TYPE_SONAR,TYPE_IR or TYPE_LASER.
     *
     * @param positions : the position of each sensor relative to belt center.
     * @param directions : the sensing ray direction of each sensor relative to
     * sensor positions. the magnitude of the vector corresponds to the max
     * range.
     */
    public IRBeltSensorBounds(Vector3d[] positions, Vector3d[] directions, float distanceSteps, Class objectType, boolean showRay) {
        int nbsensors = positions.length;
        this.distanceSteps = distanceSteps;

        // compute angles
        float radius = Float.MIN_VALUE;
        float maxRange = Float.MIN_VALUE;
        Vector3d frontVector = new Vector3d(1, 0, 0);
        angles = new double[nbsensors];
        for (int i = 0; i < nbsensors; i++) {
            Vector3d v = positions[i];
            angles[i] = v.angle(frontVector);
            double norm = v.length();
            // find the max radius
            if (norm > radius) {
                radius = (float) norm;
            }
            double range = directions[i].length();
            if (range > maxRange) {
                maxRange = (float) range;
            }

        }
        this.directions = directions;
        this.positions = positions;

        initialize(radius, maxRange, nbsensors, objectType, showRay);
    }

    private void initialize(float radius, float maxRange, int nbsensors, Class objectType, boolean showRay) {
        this.nbSensors = nbsensors;
        this.maxRange = maxRange;
        this.radius = radius;
        this.showRay = showRay;
        this.objectType = objectType;
        // reserve to avoid gc.
        t3d = new Transform3D();
        start = new Point3d();
        color = new Color3f(1.0f, 0f, 0f);

        measurements = new double[nbsensors];
        hits = new boolean[nbsensors];
        for (int i = 0; i < nbsensors; i++) {
            measurements[i] = Double.POSITIVE_INFINITY;
            hits[i] = false;
        }
        oneHasHit = false;
        create3D();

    }

    private void create3D() {
        super.create3D(true);
        // construct sensor body - a line for each individual sensor ray.
        if (showRay) {
            Point3d[] coords = new Point3d[nbSensors * 2];
            for (int i = 0; i < nbSensors; i++) {
                Point3d start = new Point3d(positions[i]);
                coords[i * 2] = start;
                Point3d end = new Point3d(start);
                Point3d direction = new Point3d(directions[i]);
                //direction.scale(0.05f); // just a small ray
                end.add(direction);
                coords[i * 2 + 1] = end;
            }
            LineArray line = new LineArray(
                    coords.length,
                    GeometryArray.COORDINATES);
            line.setCoordinates(0, coords);

            Appearance appear = new Appearance();
            Material material = new Material();

            ColoringAttributes ca = new ColoringAttributes();
            ca.setColor(color);

            appear.setColoringAttributes(ca);
            appear.setMaterial(material);
            Shape3D shape = new Shape3D(line, appear);
            shape.setCollidable(false);
            shape.setPickable(false);
            addChild(shape);
        }
    }

    protected void update() {
        if (simulator == null) {
            this.simulator = super.getOwner().getSimulator();
        }

        oneHasHit = false;
        for (int s = 0; s < nbSensors; s++) {
            hits[s] = false;
            measurements[s] = Double.POSITIVE_INFINITY;
        }

        /*
         * Build the bounds array
         */
        ArrayList<Bounds> allBounds = new ArrayList<>(simulator.getObjects().size());
        for (Object o : simulator.getObjects()) {
            if (objectType.isAssignableFrom(o.getClass())) {
                if (o instanceof SimpleAgent && o != getOwner()) {  // Agents except himself
                    SimpleAgent ag = (SimpleAgent) o;
                    BoundingSphere bs = new BoundingSphere();
                    bs.set(ag.getBounds());
                    bs.transform(ag.translation);

                    /*
                     * Point3d p = new Point3d(); bs.getCenter(p);
                     * System.out.println(objectType.getName() + ": " + p);
                     */

                    allBounds.add(bs);
                } else if (o instanceof StaticObject) {  // Objects
                    StaticObject obj = (StaticObject) o;
                    allBounds.add(obj.getTransformedBounds());
                }
            }
        }

        /*
         * Rough detection around radius
         */
        BoundingSphere roughDetection = new BoundingSphere();
        roughDetection.transform(getOwner().translation);
        roughDetection.setRadius(radius + maxRange);
        Point3d p = new Point3d();
        roughDetection.getCenter(p);
        p.y = p.y * 2;
        roughDetection.setCenter(p);
        ArrayList<Bounds> possible = new ArrayList<>(allBounds.size());
        for (Bounds b : allBounds) {
            if (b.intersect(roughDetection)) {
                possible.add(b);
            }
        }

        /*
         * Fine detection for each ray
         */
        if (!possible.isEmpty()) { // There is the possibility of one intersected
            group.getLocalToVworld(t3d);
            for (int s = 0; s < nbSensors; s++) {
                start.set(positions[s]);
                t3d.transform(start);
                Vector3d dir = new Vector3d(directions[s]);
                t3d.transform(dir);
                dir.normalize();

                ArrayList<Bounds> rayIntersected = new ArrayList<>(possible.size());
                for (Bounds b : possible) {
                    if (b.intersect(start, dir)) {
                        rayIntersected.add(b);
                    }
                }

                // something intersects this ray
                if (!rayIntersected.isEmpty()) {
                    Vector3d increment = new Vector3d(dir);
                    increment.scale(distanceSteps);
                    Point3d current = new Point3d(start);
                    boolean stop = false;
                    for (int i = 0; i < maxRange / distanceSteps && !stop; i++) {
                        for (Bounds b : rayIntersected) {
                            if (b.intersect(current)) {
                                hits[s] = true;
                                oneHasHit = true;
                                measurements[s] = i * distanceSteps;
                                stop = true;
                                break;
                            }
                        }
                        current.add(increment);
                    }
                }
            }
        }
    }

    /**
     * Returns the last measure collected for the individual sensor. Measurement
     * is made from the circle perimeter.
     *
     * @param sensorNum num of the sensor.
     * @return the range measurment.
     */
    public double getMeasurement(int sensorNum) {
        return measurements[sensorNum];
    }

    public double getFrontQuadrantMeasurement() {
        return Math.min(getQuadrantMeasurement(15 * Math.PI / 8, Math.PI * 2),
                getQuadrantMeasurement(0, Math.PI / 8));
    }

    public double getFrontRightQuadrantMeasurement() {
        return getQuadrantMeasurement(Math.PI / 8, 3 * Math.PI / 8);
    }

    public double getRightQuadrantMeasurement() {
        return getQuadrantMeasurement(3 * Math.PI / 8, 5 * Math.PI / 8);
    }

    public double getBackRightQuadrantMeasurement() {
        return getQuadrantMeasurement(5 * Math.PI / 8, 7 * Math.PI / 8);
    }

    public double getBackQuadrantMeasurement() {
        return getQuadrantMeasurement(7 * Math.PI / 8, 9 * Math.PI / 8);
    }

    public double getBackLeftQuadrantMeasurement() {
        return getQuadrantMeasurement(9 * Math.PI / 8, 11 * Math.PI / 8);
    }

    public double getLeftQuadrantMeasurement() {
        return getQuadrantMeasurement(11 * Math.PI / 8, 13 * Math.PI / 8);
    }

    public double getFrontLeftQuadrantMeasurement() {
        return getQuadrantMeasurement(13 * Math.PI / 8, 15 * Math.PI / 8);
    }

    /**
     * Returns the min measure of the sensors situated in quadrant
     * [minAngle,maxAngle].
     *
     * @param minAngle in radians the right limit of the quadrant.
     * @param maxAngle in radians the left limit of the quadrant.
     * @return the averaged measurment.
     */
    public double getQuadrantMeasurement(double minAngle, double maxAngle) {
        double m = Double.POSITIVE_INFINITY;
        for (int i = 0; i < nbSensors; i++) {
            if ((angles[i] >= minAngle) && (angles[i] <= maxAngle)) {
                if (hits[i]) {
                    m = Math.min(m, measurements[i]);
                }
            }
        }
        return m;
    }
    
    /**
     * getFrontQuadrantMeasurement(), getFrontRightQuadrantMeasurement(), 
     * getRightQuadrantMeasurement(), getBackRightQuadrantMeasurement(),
     * getBackQuadrantMeasurement(), getBackLeftQuadrantMeasurement(),
     * getLeftQuadrantMeasurement(), getFrontLeftQuadrantMeasurement()
     * @return 
     */
    public double[] getQuadrantsMeasurements() {
        double[] m = new double[]{
            getFrontQuadrantMeasurement(),
            getFrontRightQuadrantMeasurement(),
            getRightQuadrantMeasurement(),
            getBackRightQuadrantMeasurement(),
            getBackQuadrantMeasurement(),
            getBackLeftQuadrantMeasurement(),
            getLeftQuadrantMeasurement(),
            getFrontLeftQuadrantMeasurement()
        };
        return m;
    }

    public int getFrontQuadrantHits() {
        return (getQuadrantHits(15 * Math.PI / 8, Math.PI * 2) + 
                getQuadrantHits(0, Math.PI / 8));
    }

    public int getFrontRightQuadrantHits() {
        return getQuadrantHits(Math.PI / 8, 3 * Math.PI / 8);
    }

    public int getRightQuadrantHits() {
        return getQuadrantHits(3 * Math.PI / 8, 5 * Math.PI / 8);
    }

    public int getBackRightQuadrantHits() {
        return getQuadrantHits(5 * Math.PI / 8, 7 * Math.PI / 8);
    }

    public int getBackQuadrantHits() {
        return getQuadrantHits(7 * Math.PI / 8, 9 * Math.PI / 8);
    }

    public int getBackLeftQuadrantHits() {
        return getQuadrantHits(9 * Math.PI / 8, 11 * Math.PI / 8);
    }

    public int getLeftQuadrantHits() {
        return getQuadrantHits(11 * Math.PI / 8, 13 * Math.PI / 8);
    }

    public int getFrontLeftQuadrantHits() {
        return getQuadrantHits(13 * Math.PI / 8, 15 * Math.PI / 8);
    }

    /**
     * Returns number of hits in quadrant [minAngle,maxAngle].
     *
     * @param minAngle in radians the right limit of the quadrant.
     * @param maxAngle in radians the left limit of the quadrant.
     * @return the number of hits.
     */
    public int getQuadrantHits(double minAngle, double maxAngle) {
        int n = 0;
        for (int i = 0; i < nbSensors; i++) {
            if ((angles[i] >= minAngle) && (angles[i] <= maxAngle)) {
                if (hits[i]) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * Returns the hit state of the sensor.
     *
     * @param sensorNum num of the sensor.
     * @return true if the sensor ray has hit an obstacle
     */
    public boolean hasHit(int sensorNum) {
        return hits[sensorNum];
    }

    /**
     * Returns true if one of the sensors has hit.
     *
     * @return true if one ray has hit an obstacle
     */
    public boolean oneHasHit() {
        return oneHasHit;
    }

    /**
     * Return the number of individual sensor in the belt.
     *
     * @return the number of sensors.
     */
    public int getNumSensors() {
        return nbSensors;
    }

    /**
     * Returns the angle of this sensor.
     *
     * @param sensorNum - num of the sensor.
     * @return the angle in radians.
     */
    public double getSensorAngle(int sensorNum) {
        return angles[sensorNum];
    }

    /**
     * Returns the maximum sensing range in meters.
     *
     * @return the maximum range.
     */
    public float getMaxRange() {
        return maxRange;
    }

    public JPanel createInspectorPanel() {
        return new RangeSensorBeltJPanel();
    }

    /**
     * A JPanel Inner class for displaying the sensor belt rays in 2d.
     */
    private class RangeSensorBeltJPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        Font font;
        int lineSize = 8;
        DecimalFormat format;
        final static int IMAGE_SIZEX = 200;
        final static int IMAGE_SIZEY = 100;

        public RangeSensorBeltJPanel() {
            Dimension d = new Dimension(IMAGE_SIZEX, IMAGE_SIZEY);
            setPreferredSize(d);
            setMinimumSize(d);
            font = new Font("Arial", Font.PLAIN, lineSize - 1);
            // display format for numbers
            format = new DecimalFormat();
            format.setMaximumFractionDigits(3);
            format.setMinimumFractionDigits(2);
            format.setPositivePrefix("");
            format.setMinimumIntegerDigits(1);
        }

        /**
         * Caution not synchronised
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setFont(font);
            //  Color c;
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, IMAGE_SIZEX, IMAGE_SIZEY);
            g.setColor(Color.GRAY);
            int x = IMAGE_SIZEX / 2;
            int y = 0;
            for (int i = 0; i < nbSensors; i++) {
                y += lineSize;
                if (y > IMAGE_SIZEY) {
                    y = lineSize;
                    x += 50;
                }
                g.drawString("[" + i + "] " + format.format(measurements[i]), x, y);
            }
            int cx, cy;
            cx = cy = IMAGE_SIZEX / 4;
            // display factor
            float f = (float) cx / (maxRange + radius);
            int r = (int) (radius * f);
            g.setColor(Color.BLUE);
            g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
            g.drawLine(cx, cy, cx + r, cy);
            // draw each ray
            for (int i = 0; i < nbSensors; i++) {

                int x1 = (int) (positions[i].x * f);
                int y1 = (int) (positions[i].z * f);

                g.setColor(Color.RED);
                if (hits[i]) {
                    double ratio = measurements[i] / directions[i].length();

                    int x2 = x1 + (int) (directions[i].x * ratio * f);
                    int y2 = y1 + (int) (directions[i].z * ratio * f);
                    g.drawLine(cx + x1, cy + y1, cx + x2, cy + y2);
                }
            }
        }
    }
}
