/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbad.sim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.vecmath.Point3d;

/**
 *
 * @author Jorge
 */
public class CornerSensor extends SensorDevice {

    private int lastMeasurement;
    private double cornerSize;
    private Point3d p = new Point3d();
    
    public CornerSensor(double cornerSize) {
        super.create3D(true);
        this.cornerSize = cornerSize;
    }

    @Override
    protected void reset() {
        super.reset();
        lastMeasurement = 0;
    }

    @Override
    protected void update() {
        super.update();
        double worldSize = getOwner().world.worldSize;
        getOwner().getCoords(p);
        if(     (p.x < -worldSize / 2 + cornerSize && p.z < -worldSize / 2 + cornerSize) || // bottom left
                (p.x < -worldSize / 2 + cornerSize && p.z > worldSize / 2 - cornerSize) || // top left
                (p.x > worldSize / 2 - cornerSize && p.z > worldSize / 2 - cornerSize) || // top right
                (p.x > worldSize / 2 - cornerSize && p.z < -worldSize / 2 + cornerSize)) { // bottom right
            lastMeasurement = 1;
        } else {
            lastMeasurement = 0;
        }
    }
    
    public int read() {
        return lastMeasurement;
    }

    public JPanel createInspectorPanel() {
        return new ChargingPanel();
    }

    private class ChargingPanel extends JPanel {

        Font font;
        int lineSize = 11;
        int IMAGE_SIZEX = 200;
        int IMAGE_SIZEY = lineSize;

        public ChargingPanel() {
            Dimension d = new Dimension(IMAGE_SIZEX, IMAGE_SIZEY);
            setPreferredSize(d);
            setMinimumSize(d);
            font = new Font("Arial", Font.PLAIN, lineSize - 1);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, IMAGE_SIZEX, IMAGE_SIZEY);
            g.setFont(font);
            g.setColor(Color.GRAY);
            g.drawString("Task: " + lastMeasurement, 0, lineSize);
        }
    }

}
