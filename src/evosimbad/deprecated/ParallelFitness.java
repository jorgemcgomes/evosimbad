/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.deprecated.TwoStepEvolution;
import evosimbad.core.ComponentLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.encog.ml.genetic.genome.Genome;
import org.encog.ml.genetic.population.Population;
import org.encog.neural.neat.NEATNetwork;

/**
 *
 * @author jorge
 */
public class ParallelFitness extends TwoStepEvolution {

    private double allowedSimilarity;
    protected List<FitnessNEAT> fitnessEvos;

    public ParallelFitness(String novelty, String fitnessStep, int closest, double cutoff, double allowedSimilarity) {
        super(novelty, fitnessStep, closest, cutoff);
        this.allowedSimilarity = allowedSimilarity;
    }

    protected void initFitnessStep() {
        Set<Genome> pretenders = findFitGenomes();
        notifyStatus("");
        List<Set<Genome>> roots = findPopulationRoots(pretenders);
        fitnessEvos = new ArrayList<>(roots.size());
        if (main.getExtraOptions().containsKey("fitnessStepTargetSpecies")) {
            main.getExtraOptions().put("neatTargetSpecies", main.getExtraOptions().get("fitnessStepTargetSpecies"));
        }
        for (Set<Genome> root : roots) {
            try {
                notifyStatus("");
                FitnessNEAT evo = (FitnessNEAT) ComponentLoader.createInstance(FitnessNEAT.class, fitnessStep);
                Population initialPop = generatePopulation(root);
                evo.setInitialPopulation(initialPop);
                evo.setupEvolution(main);
                fitnessEvos.add(evo);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    protected List<Set<Genome>> findPopulationRoots(Set<Genome> pretenders) {
        List<Set<Genome>> roots = new ArrayList<>(pretenders.size());
        for (Genome center : pretenders) {
            Set<Genome> root = findClosest(center, closest);

            // Calculate the similarity to other roots
            double similarity = 0;
            for (Set<Genome> r : roots) {
                Set<Genome> intersection = new HashSet<>(root);
                intersection.retainAll(r);
                double sim = intersection.size() / (double) root.size();
                similarity = Math.max(similarity, sim);
            }

            // add to roots
            if (similarity < allowedSimilarity) {
                roots.add(root);
            }
        }
        return roots;
    }    

    @Override
    protected void fitnessIteration() {
        Stats iterationStats = new Stats();
        for (FitnessNEAT evo : fitnessEvos) {
            iterationStats.process(evo.fitnessStats.bestChromosome, evo.fitnessStats.bestScore);
        }
        if (iterationStats.bestScore > this.bestFitness) {
            this.bestFitness = iterationStats.bestScore;
            this.bestIndividual = (NEATNetwork) iterationStats.bestChromosome.getOrganism();
        }
        super.notifyStatus("Best: " + iterationStats.bestScore + "\tAvg best: " + iterationStats.getAverageScore());
    }
}