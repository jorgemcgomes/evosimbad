/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import evosimbad.core.Simulation;
import evosimbad.core.SimulationBuilder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import org.neat4j.neat.nn.core.NeuralNet;
import simbad.gui.Simbatch;
import simbad.sim.EnvironmentDescription;
import simbad.sim.SimpleAgent;

/**
 *
 * @author Jorge
 */
public class ImageTracer {

    private SimulationBuilder simBuilder;
    private int size = 500;

    public ImageTracer(SimulationBuilder simBuilder) {
        this.simBuilder = simBuilder;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void trace(NeuralNet individual, String name, File folder) {
        List<Simulation> exps = simBuilder.prepareExperiments(individual);
        int r = 0;
        for (Simulation s : exps) {
            /*
             * Initialization
             */
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphic = image.createGraphics();
            graphic.setBackground(Color.WHITE);
            graphic.setColor(Color.WHITE);
            graphic.fillRect(0, 0, size, size);
            Point3d t1 = new Point3d();
            EnvironmentDescription ed = s.getEnvironment();
            Simbatch simulator = new Simbatch(ed);
            ArrayList agentList = simulator.simulator.getAgentList();
            ArrayList<Point> lastPos = new ArrayList<>();

            /*
             * Mark initial positions
             */
            SimpleAgent any = (SimpleAgent) agentList.get(0);
            int markSize = (int) (any.getRadius() * 2 * size / s.getEnvironment().worldSize);
            markSize = Math.max(markSize, 5);
            for (int i = 0; i < agentList.size(); i++) {
                SimpleAgent ag = (SimpleAgent) agentList.get(i);
                ag.getCoords(t1);
                Point p = transform(t1, ed.worldSize);
                lastPos.add(p);
                graphic.setColor(ag.getColor());
                graphic.fillOval(p.x - markSize / 2, p.y - markSize / 2, markSize, markSize);
            }

            /*
             * Trace the movement
             */
            for (int i = 0; i < s.getTotalSteps(); i++) {
                simulator.step();
                for (int j = 0; j < agentList.size(); j++) {
                    SimpleAgent ag = (SimpleAgent) agentList.get(j);
                    ag.getCoords(t1);
                    Point newP = transform(t1, ed.worldSize);
                    Point lastP = lastPos.get(j);
                    graphic.setColor(ag.getColor());
                    graphic.drawLine(lastP.x, lastP.y, newP.x, newP.y);
                    lastPos.set(j, newP);
                }
            }

            /*
             * Mark final positions
             */
            for (int i = 0; i < agentList.size(); i++) {
                SimpleAgent ag = (SimpleAgent) agentList.get(i);
                ag.getCoords(t1);
                Point p = transform(t1, ed.worldSize);
                graphic.setColor(ag.getColor());
                graphic.fillRect(p.x - markSize / 2, p.y - markSize / 2, markSize, markSize);
            }

            /*
             * Save the image
             */
            File imageFile = new File(folder, name + "_" + r + ".png");
            //File imageFile = new File("C:/Users/Jorge/Dropbox", name + "_" + r + ".png");
            try {
                imageFile.getParentFile().mkdirs();
                imageFile.createNewFile();
                ImageIO.write(image, "png", imageFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            r++;
        }
    }

    private Point transform(Point3d point3d, float worldSize) {
        Point p = new Point();
        // translate to positive values
        point3d.x = point3d.x + worldSize / 2;
        point3d.z = point3d.z + worldSize / 2;
        // scale
        p.x = (int) Math.round(size * point3d.x / worldSize);
        p.y = (int) Math.round(size * point3d.z / worldSize);
        return p;
    }
}
