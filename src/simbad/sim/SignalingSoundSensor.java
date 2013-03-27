/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbad.sim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.media.j3d.Transform3D;
import javax.swing.JPanel;
import javax.vecmath.Point3d;

/**
 *
 * @author Jorge
 */
public class SignalingSoundSensor extends SensorDevice {

    private double[] measurements;
    private double maxDistance;
    private Point3d[] micros;
    public static final int FRONT_RIGHT = 0;
    public static final int FRONT_LEFT = 1;
    public static final int BACK = 2;

    /**
     * @param radius
     * @param arrayCount Must be an even number
     * @param maxDistance 
     */
    public SignalingSoundSensor(double radius, double maxDistance) {
        this.maxDistance = maxDistance;
        initialize(radius);
    }

    private void initialize(double radius) {
        micros = new Point3d[3];
        Point3d front = new Point3d(radius, 0, 0);
        Transform3D transform = new Transform3D();

        transform.setIdentity();
        transform.rotY(Math.PI / 3); // front right
        micros[FRONT_RIGHT] = new Point3d(front);
        transform.transform(micros[FRONT_RIGHT]);

        transform.setIdentity();
        transform.rotY(-Math.PI / 3); // front left
        micros[FRONT_LEFT] = new Point3d(front);
        transform.transform(micros[FRONT_LEFT]);

        transform.setIdentity();
        transform.rotY(Math.PI); // back
        micros[BACK] = new Point3d(front);
        transform.transform(micros[BACK]);

        create3D();
    }

    /**
     * http://stason.org/TULARC/physics/acoustics-faq/2-9-How-does-sound-decay-with-distance.html
     * http://www.sengpielaudio.com/calculator-distancelaw.htm
     */
    protected void update() {
        ArrayList agentList = super.getOwner().getSimulator().getAgentList();
        for (int i = 0; i < micros.length; i++) {
            double measurement = 0;
            for (Object o : agentList) {
                Agent agent = (Agent) o;
                if (agent != super.getOwner()) {
                    Point3d agentPos = new Point3d();
                    agent.getCoords(agentPos);
                    double dist = agentPos.distance(micros[i]);
                    if (dist < maxDistance) // sound level = 1/(dist+1)^2 ; ]0,1] para dist > 0
                    {
                        measurement += 1 / Math.pow(dist + 0.5, 2);
                    }
                }
            }
            measurements[i] = measurement / (agentList.size() - 1) / 2;
        }
    }

    protected void reset() {
        measurements = new double[micros.length];
    }

    public double getMeasurement(int i) {
        return measurements[i];
    }

    public double getMaxValue() {
        return 1;
    }

    public JPanel createInspectorPanel() {
        return new SoundPanel();
    }

    private class SoundPanel extends JPanel {

        Font font;
        int lineSize = 11;
        DecimalFormat format;
        int IMAGE_SIZEX = 200;
        int IMAGE_SIZEY = 33;

        public SoundPanel() {
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

        /** Caution not synchronised */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, IMAGE_SIZEX, IMAGE_SIZEY);
            g.setFont(font);
            g.setColor(Color.GRAY);
            g.drawString("FrontRight: " + format.format(measurements[FRONT_RIGHT]), 0, lineSize);
            g.drawString("FrontLeft: " + format.format(measurements[FRONT_LEFT]), 0, lineSize * 2);
            g.drawString("Back: " + format.format(measurements[BACK]), 0, lineSize * 3);
        }
    }

    private void create3D() {
        super.create3D(true);
        /*Color3f color = new Color3f(0.2f, 0.2f, 0.2f);
        Appearance appear = new Appearance();
        appear.setMaterial(new Material(color, black, color, white, 100.0f));
        for (Point3d p : micros) {
        Node node = new com.sun.j3d.utils.geometry.Box(0.008f, 0.008f, 0.008f, appear);
        node.setCollidable(false);
        node.setPickable(false);
        addChild(node);
        }*/
    }
}