/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.simulation.test;

import evosimbad.simulation.test.GridSimulator;
import java.util.List;
import java.util.Scanner;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFTask;
import simbad.gui.Simbatch;

/**
 *
 * @author Jorge
 */
public class GridFlusher {

    public static final int CLEAN_FLUSH_SIZE = 200; // in number of tasks

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Specify client UUID: ");
        String uuid = sc.nextLine();
        
        JPPFClient gridClient = new JPPFClient(uuid);

        // CLEAN THE RESOURCES
        JPPFJob cleaning = new JPPFJob();
        cleaning.setId("Cleaning");
        for (int i = 0; i < CLEAN_FLUSH_SIZE; i++) {
            try {
                cleaning.addTask(new CleaningTask());
            } catch (JPPFException ex) {
                ex.printStackTrace();
            }
        }
        try {
            List<JPPFTask> submit = gridClient.submit(cleaning);
            for(JPPFTask t : submit) {
                if(t.getException() != null) {
                    t.getException().printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static class CleaningTask extends JPPFTask {

        @Override
        public void run() {
            for (Simbatch sim : GridSimulator.simulators.values()) {
                sim.dispose();
            }
            GridSimulator.simulators.clear();
            GridSimulator.experimentCount.clear();
            System.gc();
            System.out.println("SIMULATORS CLEAN");
        }
    }
}
