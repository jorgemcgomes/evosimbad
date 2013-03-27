/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.simulation;

import evosimbad.core.EvaluationFunction;
import evosimbad.core.Simulation;
import evosimbad.core.Simulator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang3.tuple.Pair;
import org.neat4j.neat.nn.core.NeuralNet;
import simbad.gui.Simbatch;

/**
 *
 * @author Jorge
 */
public class MultiThreadedSimulator extends Simulator {

    private ExecutorService threadPool;

    public MultiThreadedSimulator() {
        this(0);
    }

    public MultiThreadedSimulator(int threads) {
        super();
        int nThreads = threads > 0 ? threads : Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public HashMap<NeuralNet, List<EvaluationFunction>> runExperiments(Set<NeuralNet> controllers) {
        HashMap<NeuralNet, List<EvaluationFunction>> results = new HashMap<>(controllers.size() * 2);
        // Execute the experiments
        ArrayList<Future<Pair<NeuralNet, List<EvaluationFunction>>>> futures = new ArrayList<>(controllers.size());
        for (NeuralNet contr : controllers) {
            futures.add(threadPool.submit(new IndividualEvaluation(contr)));
        }
        // Wait for the termination
        for (Future<Pair<NeuralNet, List<EvaluationFunction>>> f : futures) {
            try {
                Pair<NeuralNet, List<EvaluationFunction>> get = f.get();
                results.put(get.getLeft(), get.getRight());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        //System.gc();
        System.out.println();
        return results;
    }

    private class IndividualEvaluation implements Callable<Pair<NeuralNet, List<EvaluationFunction>>> {

        private NeuralNet individual;

        IndividualEvaluation(NeuralNet ind) {
            this.individual = ind;
        }

        @Override
        public Pair<NeuralNet, List<EvaluationFunction>> call() throws Exception {
            List<Simulation> exps = simBuilder.prepareExperiments(individual);
            List<EvaluationFunction> evals = new ArrayList<>(exps.size());
            for (Simulation exp : exps) {
                Simbatch sim = new Simbatch(exp.getEnvironment());
                sim.simulator.setFramesPerSecond(exp.getFPS());
                sim.reset();
                for (int i = 0; i < exp.getTotalSteps(); i++) {
                    exp.step();
                    sim.step();
                }
                exp.done();
                sim.dispose();
                evals.add(exp.getEvaluationFunction());
            }
            System.out.print(".");
            return Pair.of(individual, evals);
        }
    }
}
