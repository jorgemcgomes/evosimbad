/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.util.HashSet;
import java.util.Set;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 *
 * @author Jorge
 */
public abstract class EvolutionMethod {

    protected ComponentLoader main;
    protected NeuralNet bestIndividual;
    protected double bestFitness;
    protected int trainIteration;
    protected int maxIterations;
    protected long elapsedTime;
    private long averageTimePerGen = 0;
    protected boolean isEvolving;
    protected volatile boolean stopEvolution;
    protected Set<EvolutionProgressListener> listeners = new HashSet<>();

    public EvolutionMethod(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setupEvolution(ComponentLoader exp) {
        this.bestIndividual = null;
        this.bestFitness = Double.NEGATIVE_INFINITY;
        this.main = exp;
        this.trainIteration = 0;
        this.stopEvolution = false;
        this.elapsedTime = 0;
        this.isEvolving = false;

        notifyIterationChanged();
    }

    public void addProgressListener(EvolutionProgressListener listener) {
        listeners.add(listener);
    }

    public void removeProgressListener(EvolutionProgressListener listener) {
        listeners.remove(listener);
    }

    protected void notifyStatus(String message) {
        for (EvolutionProgressListener l : listeners) {
            l.statusMessage(message);
        }
    }

    protected void notifyIterationChanged() {
        for (EvolutionProgressListener l : listeners) {
            l.iterationChanged(trainIteration, maxIterations);
        }
    }

    protected void evolutionStopped() {
        main.getLogger().flush();
        for (EvolutionProgressListener l : listeners) {
            l.evolutionStoped();
        }
    }

    public void evolve() {
        stopEvolution = false;
        isEvolving = true;
        while (!stopEvolution && trainIteration < maxIterations) {
            main.getLogger().flush();
            long t = System.currentTimeMillis();
            evolutionIteration();
            t = System.currentTimeMillis() - t;
            System.out.println("Last time: " + t);
            elapsedTime += t;
            averageTimePerGen = t;
            notifyIterationChanged();
        }
        isEvolving = false;
        evolutionStopped();
    }

    public void stopEvolution() {
        stopEvolution = true;
    }

    public boolean isEvolving() {
        return isEvolving;
    }

    public NeuralNet getBestIndividual() {
        return bestIndividual;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public long averageTimePerGeneration() {
        return averageTimePerGen;
    }

    public long totalEvolutionTime() {
        return elapsedTime;
    }

    public long estimatedTimeArrival() {
        return averageTimePerGeneration() * (maxIterations - trainIteration);
    }

    public int getMaxIterations() {
        return maxIterations;
    }
    
    public int getTrainIteration() {
        return trainIteration;
    }

    public void evolutionIteration() {
        trainIteration++;
    }
    
    public NEATNeuralNet decodeChromosome(Chromosome c) {
        NEATNetDescriptor descr = new NEATNetDescriptor(0, null);
        descr.updateStructure(c);
        NEATNeuralNet net = new NEATNeuralNet();
        net.createNetStructure(descr);
        net.updateNetStructure();
        return net;
    }    
    
    public static class Stats {

        public double bestScore = Double.NEGATIVE_INFINITY;
        public Chromosome bestChromosome = null;
        private double totalScore = 0;
        public double worstScore = Double.POSITIVE_INFINITY;
        public Chromosome worstChromosome = null;
        private int count;

        public void process(Chromosome c, double score) {
            if (score > bestScore) {
                bestScore = score;
                bestChromosome = c;
            }
            if (score < worstScore) {
                worstScore = score;
                worstChromosome = c;
            }
            totalScore += score;
            count++;
        }

        public double getAverageScore() {
            return totalScore / count;
        }
    }    
}
