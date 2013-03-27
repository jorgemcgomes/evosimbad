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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.media.j3d.VirtualUniverse;
import org.encog.ml.MLRegression;
import simbad.gui.Simbatch;

/**
 *
 * @author Jorge
 */
public class MultiThreadedSimulator extends Simulator {

    private HashMap<Thread, Simbatch> simulators;
    private ExecutorService threadPool;
    public static final int REFRESH_INTERVAL = 1000;
    private int counter = 0;

    public MultiThreadedSimulator(int threads) {
        super();
        int nThreads = threads > 0 ? threads : Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(nThreads);
        simulators = new HashMap<>(nThreads);
    }

    @Override
    public HashMap<MLRegression, List<EvaluationFunction>> runExperiments(Set<MLRegression> controllers) {
        // Cleanup
        if (counter > REFRESH_INTERVAL) {
            System.out.println("CLEANING: " + Runtime.getRuntime().totalMemory() / 1000);
            for (Simbatch sim : simulators.values()) {
                sim.dispose();
            }
            simulators.clear();
            counter = 0;
            System.gc();
        }
        
        // Prepare experiments and results
        List<Simulation> allExps = new LinkedList<>();
        SimulationBuilder simBuilder = main.getSimulationBuilder();
        HashMap<MLRegression, List<EvaluationFunction>> results = new HashMap<>(controllers.size() * 2);
        for(MLRegression net : controllers) {
            List<Simulation> exps = simBuilder.prepareExperiments(net);
            allExps.addAll(exps);
            
            List<EvaluationFunction> evals = new ArrayList<>(exps.size());
            for(Simulation e : exps) {
                evals.add(e.getEvaluationFunction());
            }
            results.put(net, evals);
        }
        
        // Execute the experiments
        ArrayList<Future<?>> futures = new ArrayList<>(allExps.size());
        for (Simulation exp : allExps) {
            futures.add(threadPool.submit(new ExperimentRun(exp)));
        }
        // Wait for the termination
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception ex) {
                ex.printStackTrace();
                counter = REFRESH_INTERVAL + 1; // force cleaning next time
            }
        }
        System.out.println();        
        counter += allExps.size();
        
        return results;
    }

    private class ExperimentRun implements Runnable {

        private Simulation exp;

        ExperimentRun(Simulation exp) {
            this.exp = exp;
        }

        @Override
        public void run() {
            Thread current = Thread.currentThread();
            Simbatch sim = simulators.get(current);
            if (sim == null) {
                sim = new Simbatch(exp.getEnvironment());
                sim.reset();
                sim.simulator.setFramesPerSecond(exp.getFPS());
                simulators.put(current, sim);
            } else {
                sim.changeEnvironment(exp.getEnvironment());
                sim.reset();
            }
            /*current.setPriority(Thread.MIN_PRIORITY);
            VirtualUniverse.setJ3DThreadPriority(Thread.MAX_PRIORITY);*/
            for (int i = 0; i < exp.getTotalSteps(); i++) {
                exp.step();
                sim.step();
            }
            System.out.print(".");
        }
    }
}
