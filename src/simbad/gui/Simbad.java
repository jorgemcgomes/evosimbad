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
 * $Date: 2005/08/07 12:24:56 $
 * $Revision: 1.14 $
 * $Source: /cvsroot/simbad/src/simbad/gui/Simbad.java,v $
 */
package simbad.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import java.util.HashMap;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import simbad.sim.Agent;
import simbad.sim.EnvironmentDescription;
import simbad.sim.Simulator;
import simbad.sim.World;
//import javax.swing.UIManager;

/**
 * This is the Simbad application mainframe.
 *  
 */
public class Simbad extends JFrame {

    private static final long serialVersionUID = 1L;
    static final String version = "1.4";
    static int SIZEX = 1000;
    static int SIZEY = 800;
    JMenuBar menubar;
    JDesktopPane desktop;
    WorldWindow worldWindow = null;
    ControlWindow controlWindow = null;
    World world;
    public Simulator simulator;
    Console console = null;
    HashMap<Agent, AgentInspector> agentInspectors = new HashMap<>();
    boolean backgroundMode;
    static Simbad simbadInstance = null;
    private int fps;

    /** Construct Simbad application with the given environement description */
    public Simbad(EnvironmentDescription ed, boolean backgroundMode, int fps) {
        super("Simbad  - version " + version);
        simbadInstance = this;
        this.fps = fps;
        this.backgroundMode = backgroundMode;
        desktop = new JDesktopPane();
        setSize(SIZEX, SIZEY);
        createGUI();
        start(ed);
        setVisible(true);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                simulator.dispose();
                world.dispose();
                setVisible(false);
                dispose();
            }
        });
    }

    /** Create the main GUI. Only called once.*/
    private void createGUI() {
        desktop.setFocusable(true);
        getContentPane().add(desktop);
        menubar = new JMenuBar();
        setJMenuBar(menubar);
    }
    
    private JMenu createInspectorsMenu() {
        JMenu menu = new JMenu("Agents");
        ArrayList agentList = simulator.getAgentList();
        for(Object obj : agentList) {
            final Agent ag = (Agent) obj;
            JMenuItem item = new JMenuItem(ag.getName());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AgentInspector inspector = agentInspectors.get(ag);
                    desktop.add(inspector);
                    inspector.show();
                    inspector.setLocation(0, 0);
                }
            });
            menu.add(item);
        }
        return menu;
    }

    /** Starts (or Restarts after releaseRessources) the world and simulator.*/
    private void start(EnvironmentDescription ed) {
        System.out.println("Starting environmentDescription: " + ed.getClass().getName());
        world = new World(ed, false);
        simulator = new Simulator(desktop, world, ed);
        simulator.setFramesPerSecond(fps);
        createInternalFrames();
        if (backgroundMode) {
            runBackgroundMode();
        }
    }

    /** Release all ressources. */
    private void releaseRessources() {
        simulator.dispose();
        world.dispose();
        disposeInternalFrames();
    }

    /**
     * Creates the windows as Swing InternalFrames
     */
    private void createInternalFrames() {
        worldWindow = new WorldWindow(world);
        desktop.add(worldWindow);
        worldWindow.show();
        worldWindow.setLocation(230, 0);
        createAgentInspectors();
        menubar.add(createInspectorsMenu());
        if (!backgroundMode) {
            controlWindow = new ControlWindow(world, simulator);
            desktop.add(controlWindow);
            controlWindow.show();
            controlWindow.setLocation(230, 600);
        }
    }

    /**
     * Dispose the windows- used before restart.
     */
    private void disposeInternalFrames() {
        simulator.dispose();
        worldWindow.dispose();
        for(AgentInspector ai : agentInspectors.values()) {
            ai.dispose();
        }
        if (controlWindow != null) {
            controlWindow.dispose();
        }
    }

    /**
     * creates agent inspector window
     */
    private void createAgentInspectors() {
        ArrayList agents = simulator.getAgentList();
        for (Object a : agents) {
            if (a instanceof Agent) {
                Agent ag = (Agent) a;
                AgentInspector ai = new AgentInspector((Agent) a, !backgroundMode, simulator);
                agentInspectors.put(ag, ai);
            }
        }
    }

    /** 
     * Runs Simbad in background mode for computation intensive application. 
     * Minimize graphic display and renderer computation.
     */
    private void runBackgroundMode() {
        //TODO pb with collision , pb with camera in this mode.
        setTitle(this.getTitle() + " - Background Mode");
        System.out.println("---------------------------------------------");
        System.out.println("Simbad is running in 'Background Mode");
        System.out.println("World is rendered very rarely. UI is disabled");
        System.out.println("--------------------------------------------");
        // slow down
        for(AgentInspector ai : agentInspectors.values()) {
            ai.setFramesPerSecond(0.5f);
        }
        // Show a small indication window
        JInternalFrame frame = new JInternalFrame();
        JPanel p = new JPanel();
        p.add(new JLabel("BACKGROUND MODE"));
        frame.setContentPane(p);
        frame.setClosable(false);
        frame.pack();
        frame.setLocation(SIZEX / 2, SIZEY * 3 / 4);
        desktop.add(frame);
        frame.show();
        world.changeViewPoint(World.VIEW_FROM_TOP, null);
        // start
        simulator.startBackgroundMode();
    }

    public JDesktopPane getDesktopPane() {
        return desktop;
    }

    /////////////////////////
    // Class methods
    public static Simbad getSimbadInstance() {
        return simbadInstance;
    }
}