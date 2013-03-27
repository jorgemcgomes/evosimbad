/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbad.sim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.vecmath.Point3d;

/**
 *
 * @author Jorge
 */
public class NearCountSensor extends SensorDevice {

    private double maxDistance;
    private int lastMeasurement;
    private int maxCount;
    private Class robotClass;

    /**
     * @param maxDistance
     * @param robotClass
     * @param maxCount < 0 for normalization respective to the size of the swarm
     */
    public NearCountSensor(double maxDistance, Class robotClass, int maxCount) {
        this.maxDistance = maxDistance;
        this.robotClass = robotClass;
        this.maxCount = maxCount;
        super.create3D(true);
    }

    @Override
    protected void reset() {
        super.reset();
        lastMeasurement = 0;
    }

    @Override
    protected void update() {
        super.update();
        int count = 0;
        Point3d selfPos = new Point3d();
        getOwner().getCoords(selfPos);
        for (Object o : getOwner().getSimulator().getAgentList()) {
            if (o != getOwner() && robotClass.isAssignableFrom(o.getClass())) {
                Agent ag = (Agent) o;
                Point3d otherPos = new Point3d();
                ag.getCoords(otherPos);
                if (selfPos.distance(otherPos) < maxDistance) {
                    count++;
                }
            }
        }
        lastMeasurement = count;
    }

    public double getNearCount() {
        return lastMeasurement;
    }

    public double getNearCountNormalized() {
        ArrayList agentList = getOwner().getSimulator().getAgentList();
        if (agentList.size() > 1 && maxCount < 0) {
            maxCount = 0;
            for (Object o : agentList) {
                if(o != getOwner() && robotClass.isAssignableFrom(o.getClass())) {
                    maxCount++;
                }
            }
        }
        return lastMeasurement / maxCount;
    }

    public JPanel createInspectorPanel() {
        return new NearRobotPanel();
    }

    private class NearRobotPanel extends JPanel {

        Font font;
        int lineSize = 11;
        int IMAGE_SIZEX = 200;
        int IMAGE_SIZEY = lineSize;

        public NearRobotPanel() {
            Dimension d = new Dimension(IMAGE_SIZEX, IMAGE_SIZEY);
            setPreferredSize(d);
            setMinimumSize(d);
            font = new Font("Arial", Font.PLAIN, lineSize - 1);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, IMAGE_SIZEX, IMAGE_SIZEY);
            g.setFont(font);
            g.setColor(Color.GRAY);
            g.drawString("Near robots: " + lastMeasurement, 0, lineSize);
        }
    }
}
