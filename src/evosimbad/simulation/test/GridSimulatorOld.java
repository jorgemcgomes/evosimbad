/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.simulation.test;

import evosimbad.core.EvaluationFunction;
import evosimbad.core.SimulationBuilder;
import evosimbad.core.Simulator;
import evosimbad.core.Simulation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.media.j3d.VirtualUniverse;
import org.encog.ml.MLRegression;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFTask;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;
import simbad.gui.Simbatch;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author Jorge
 */
public class GridSimulatorOld extends Simulator {

    private JPPFClient gridClient;
    private int idCount = 0;
    private int chunkSize;

    public GridSimulatorOld(int chunkSize) {
        gridClient = new JPPFClient();
        this.chunkSize = chunkSize;
    }

    @Override
    public HashMap<MLRegression, List<EvaluationFunction>> runExperiments(Set<MLRegression> controllers) {
        // Grid structures initialization
        DataProvider dataProvider = new MemoryMapDataProvider();
        try {
            dataProvider.setValue("builder", main.getSimulationBuilder());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JPPFJob job = new JPPFJob(dataProvider);
        job.setId("evosimbad" + idCount++);
        job.setBlocking(true);

        // Build chunks and add tasks to job
        ArrayList<ArrayList<MLRegression>> chunks = new ArrayList<>(controllers.size() / chunkSize + 1);
        LinkedList<MLRegression> rList = new LinkedList<>(controllers);
        while (!rList.isEmpty()) {
            ArrayList<MLRegression> chunk = new ArrayList<>(chunkSize);
            for (int i = 0; i < chunkSize && !rList.isEmpty(); i++) {
                chunk.add(rList.pollFirst());
            }
            try {
                job.addTask(new SimulationTask(chunk, chunks.size()));
            } catch (JPPFException ex) {
                ex.printStackTrace();
            }
            chunks.add(chunk);
        }

        // Execute tasks and collect results
        HashMap<MLRegression, List<EvaluationFunction>> result = new HashMap<>(controllers.size() * 2);
        System.out.println("Sending to grid " + job.getTasks().size() + " tasks");
        try {
            List<JPPFTask> results = gridClient.submit(job);
            for (JPPFTask t : results) {
                SimulationTask task = (SimulationTask) t;
                if (task.getException() != null) {
                    System.out.println(t.getException().getMessage());
                }
                ArrayList<ArrayList<EvaluationFunction>> r = (ArrayList<ArrayList<EvaluationFunction>>) t.getResult();
                ArrayList<MLRegression> chunk = chunks.get(task.id); // find the chunk respective to the task
                Iterator<ArrayList<EvaluationFunction>> evalIter = r.iterator();
                Iterator<MLRegression> regIter = chunk.iterator();
                while (evalIter.hasNext()) {
                    result.put(regIter.next(), evalIter.next());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static class SimulationTask extends JPPFTask {

        private ArrayList<MLRegression> networks;
        private int id;
        private transient Simbatch simulator;
        private transient SimulationBuilder builder;

        SimulationTask(ArrayList<MLRegression> networks, int id) {
            this.networks = networks;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                this.builder = (SimulationBuilder) getDataProvider().getValue("builder");
                System.out.println(Thread.currentThread().getName() + " pre-simulator");
                this.simulator = new Simbatch(new EnvironmentDescription());
                //createSimulator();
                System.out.println(Thread.currentThread().getName() + " pos-simulator");

                ArrayList<ArrayList<EvaluationFunction>> result = new ArrayList<>(networks.size());
                for (MLRegression network : networks) {
                    result.add(evaluateNetwork(network));
                }

                simulator.dispose();
                super.setResult(result);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        /*private void createSimulator() {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    simulator = new Simbatch(new EnvironmentDescription());
                }
            });
            t.start();
            try {
                t.join(1000);
                if(t.isAlive()) {
                    System.out.println("TRYING TO CREATE SIMULATOR ONE MORE TIME");
                    t.stop();
                    Thread.sleep(1000);
                    createSimulator();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }*/

        private ArrayList<EvaluationFunction> evaluateNetwork(MLRegression network) {
            List<Simulation> experiments = builder.prepareExperiments(network);
            ArrayList<EvaluationFunction> evals = new ArrayList<>(experiments.size());
            for (Simulation e : experiments) {
                runExperiment(e);
                evals.add(e.getEvaluationFunction());
            }
            return evals;
        }

        private void runExperiment(Simulation exp) {
            simulator.simulator.setFramesPerSecond(exp.getFPS());
            simulator.changeEnvironment(exp.getEnvironment());
            simulator.reset();
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            VirtualUniverse.setJ3DThreadPriority(Thread.MAX_PRIORITY);
            for (int i = 0; i < exp.getTotalSteps(); i++) {
                exp.step();
                simulator.step();
            }
        }
    }
}
