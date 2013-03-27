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

/**
 *
 * @author jorge
 */
public class DummySensor<T> extends SensorDevice {

    public interface SensorReader<T> {

        public T getReading();
    }
    private T lastMeasurement;
    private SensorReader<T> reader;
    private String name;
    private T defaultValue;

    public DummySensor(String name, T defaultValue, SensorReader<T> reader) {
        super.create3D(true);
        this.name = name;
        this.reader = reader;
        this.defaultValue = defaultValue;
        this.lastMeasurement = defaultValue;
    }

    public T read() {
        return lastMeasurement;
    }

    @Override
    protected void reset() {
        super.reset();
        lastMeasurement = defaultValue;
    }

    @Override
    protected void update() {
        super.update();
        lastMeasurement = reader.getReading();
    }

    @Override
    public JPanel createInspectorPanel() {
        return new DummySensor.InfoPanel();
    }

    private class InfoPanel extends JPanel {

        Font font;
        int lineSize = 11;
        int IMAGE_SIZEX = 200;
        int IMAGE_SIZEY = lineSize;

        public InfoPanel() {
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
            g.drawString(name + ": " + lastMeasurement, 0, lineSize);
        }
    }
}
