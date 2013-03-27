/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.simulation.test;

import evosimbad.core.EvaluationFunction;
import evosimbad.core.EvolutionProgressListener;
import evosimbad.core.SimulationBuilder;
import evosimbad.core.Simulator;
import evosimbad.core.Simulation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class GridSimulator extends Simulator {

    public static Map<String, Simbatch> simulators = Collections.synchronizedMap(new HashMap<String, Simbatch>());
    public static Map<String, Integer> experimentCount = Collections.synchronizedMap(new HashMap<String, Integer>());
    public static final int REFRESH_INTERVAL = 400; // in simulations made
    private JPPFClient gridClient;
    private int idCount = 0;

    public GridSimulator() {
        gridClient = new JPPFClient();
    }
    
    public GridSimulator(String uuid) {
        gridClient = new JPPFClient(uuid);
    }

    @Override
    public HashMap<MLRegression, List<EvaluationFunction>> runExperiments(Set<MLRegression> controllers) {
        DataProvider dataProvider = new MemoryMapDataProvider();
        try {
            dataProvider.setValue("builder", main.getSimulationBuilder());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JPPFJob job = new JPPFJob(dataProvider);
        job.setId("evosimbad" + idCount++);

        ArrayList<MLRegression> rList = new ArrayList<>(controllers.size());
        for (MLRegression r : controllers) {
            try {
                job.addTask(new SimulationTask(r, rList.size()));
                rList.add(r);
            } catch (JPPFException ex) {
                ex.printStackTrace();
            }
        }
        job.setBlocking(true);


        HashMap<MLRegression, List<EvaluationFunction>> res = new HashMap<>(controllers.size() * 2);
        try {
            System.out.println("Sending to grid " + job.getTasks().size() + " tasks");
            List<JPPFTask> results = gridClient.submit(job);
            for (JPPFTask t : results) {
                SimulationTask task = (SimulationTask) t;
                if (t.getException() != null) {
                    System.out.println(t.getException().getMessage());
                }
                ArrayList<EvaluationFunction> evals = (ArrayList<EvaluationFunction>) task.getResult();
                if (evals == null) {
                    System.out.println("NULL LIST");
                }

                res.put(rList.get(task.id), evals);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return res;
    }

    public static class SimulationTask extends JPPFTask {

        private MLRegression network;
        private int id;

        SimulationTask(MLRegression network, int id) {
            this.network = network;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                DataProvider dataProvider = getDataProvider();
                SimulationBuilder builder = (SimulationBuilder) dataProvider.getValue("builder");
                List<Simulation> experiments = builder.prepareExperiments(network);
                ArrayList<EvaluationFunction> evals = new ArrayList<>(experiments.size());

                String threadName = Thread.currentThread().getName();
                Simbatch sim = simulators.get(threadName);
                if (sim == null) {
                    sim = new Simbatch(new EnvironmentDescription());
                    Thread.sleep(1000);
                    simulators.put(threadName, sim);
                    System.out.println("simulator created in " + threadName);
                }
                int actualCount = experimentCount.containsKey(threadName) ? experimentCount.get(threadName) : 0;
                int newCount = actualCount + experiments.size();
                experimentCount.put(threadName, newCount);

                for (Simulation e : experiments) {
                    runExperiment(sim, e);
                    evals.add(e.getEvaluationFunction());
                }
                super.setResult(evals);

                if (newCount > REFRESH_INTERVAL) {
                    System.out.println("CLEANING: " + Runtime.getRuntime().totalMemory() / 1000);
                    sim.dispose();
                    simulators.remove(threadName);
                    experimentCount.put(threadName, 0);
                    System.gc();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                super.setException(ex);
            }
        }

        private void runExperiment(Simbatch sim, Simulation exp) {
            sim.simulator.setFramesPerSecond(exp.getFPS());
            sim.changeEnvironment(exp.getEnvironment());
            sim.reset();
            //Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            //VirtualUniverse.setJ3DThreadPriority(Thread.MAX_PRIORITY);
            for (int i = 0; i < exp.getTotalSteps(); i++) {
                exp.step();
                sim.step();
            }
        }
    }
}
