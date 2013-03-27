/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.simulation;

import evosimbad.core.EvaluationFunction;
import evosimbad.core.Simulation;
import evosimbad.core.SimulationBuilder;
import evosimbad.core.Simulator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFExceptionResult;
import org.jppf.server.protocol.JPPFTask;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;
import org.neat4j.neat.nn.core.NeuralNet;
import simbad.gui.Simbatch;

/**
 *
 * @author Jorge
 */
public class GridSimulator extends Simulator {

    private JPPFClient gridClient;
    private int idCount = 0;
    private int randomID;

    public GridSimulator() {
        gridClient = new JPPFClient();
        randomID = (int) (Math.random() * 8999) + 1000;
        System.out.println("Job id: " + randomID);
    }
    
    public GridSimulator(String uuid) {
        gridClient = new JPPFClient(uuid);
        randomID = (int) (Math.random() * 8999) + 1000;
        System.out.println("Job id: " + randomID);
    }

    @Override
    public HashMap<NeuralNet, List<EvaluationFunction>> runExperiments(Set<NeuralNet> controllers) {
        DataProvider dataProvider = new MemoryMapDataProvider();
        try {
            dataProvider.setValue("builder", simBuilder);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JPPFJob job = new JPPFJob(dataProvider);
        job.setId("evosimbad_" + randomID + "_" + idCount++);

        ArrayList<NeuralNet> rList = new ArrayList<>(controllers.size());
        for (NeuralNet r : controllers) {
            try {
                job.addTask(new SimulationTask(r, rList.size()));
                rList.add(r);
            } catch (JPPFException ex) {
                ex.printStackTrace();
            }
        }
        job.setBlocking(true);


        HashMap<NeuralNet, List<EvaluationFunction>> res = new HashMap<>(controllers.size() * 2);
        try {
            System.out.println("Sending to grid " + job.getTasks().size() + " tasks");
            List<JPPFTask> results = gridClient.submit(job);
            for (JPPFTask t : results) {
                if(t instanceof JPPFExceptionResult) {
                    JPPFExceptionResult ex = (JPPFExceptionResult) t;
                    ex.getException().printStackTrace();
                }
                SimulationTask task = (SimulationTask) t;
                if (t.getException() != null) {
                    System.out.println(t.getException().getMessage());
                    t.getException().printStackTrace();
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

        private NeuralNet network;
        private int id;

        SimulationTask(NeuralNet network, int id) {
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

                for (Simulation e : experiments) {
                    runExperiment(e);
                    evals.add(e.getEvaluationFunction());
                }
                super.setResult(evals);
            } catch (Exception ex) {
                ex.printStackTrace();
                super.setException(ex);
            }
        }

        private void runExperiment(Simulation exp) {
            Simbatch sim = new Simbatch(exp.getEnvironment());
            sim.simulator.setFramesPerSecond(exp.getFPS());
            sim.reset();
            for (int i = 0; i < exp.getTotalSteps(); i++) {
                exp.step();
                sim.step();
            }
            exp.done();
            sim.dispose();
        }
    }
}
