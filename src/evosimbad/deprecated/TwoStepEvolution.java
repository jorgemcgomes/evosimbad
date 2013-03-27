/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvolutionMethod;
import evosimbad.core.EvolutionProgressListener;
import evosimbad.evolution.FitnessNEAT;
import evosimbad.evolution.NoveltyNEAT;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map.Entry;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.neat4j.neat.core.InnovationDatabase;
import org.neat4j.neat.core.NEATChromosome;
import org.neat4j.neat.core.NEATGADescriptor;
import org.neat4j.neat.core.mutators.NEATMutator;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class TwoStepEvolution extends EvolutionMethod {

    protected NoveltyNEAT noveltyEvo;
    protected FitnessNEAT fitnessEvo;
    protected String noveltyStep, fitnessStep;
    protected String noveltyPath;
    protected HashMap<Chromosome, Pair<Double, double[]>> repository;
    protected double cutoff;
    protected int closest;
    private boolean fitnessStepStarted = false;

    public TwoStepEvolution(String noveltyStep, String fitnessStep, String exp, int closest, double cutoff) {
        super(0);
        if (exp != null && new File(exp).exists()) {
            this.noveltyPath = exp;
        }
        this.noveltyStep = noveltyStep;
        this.fitnessStep = fitnessStep;
        this.cutoff = cutoff;
        this.closest = closest;
    }

    @Override
    public void setupEvolution(ComponentLoader loader) {
        super.setupEvolution(loader);

        // load repo
        if (noveltyPath != null) {
            try {
                // Load repository
                File repo = new File(noveltyPath, "genomeRepository.ser");
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(repo));
                repository = (HashMap<Chromosome, Pair<Double, double[]>>) ois.readObject();
                ois.close();

                // Restore innovation database
                File innovationDB = new File(noveltyPath, "innovationDB.ser");
                ois = new ObjectInputStream(new FileInputStream(innovationDB));
                InnovationDatabase.restoreInnovationDatabase(ois.readObject());
                ois.close();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            System.out.println("repository loaded");
        }

        // init novelty evolution
        try {
            noveltyEvo = (NoveltyNEAT) ComponentLoader.createInstance(NoveltyNEAT.class, noveltyStep);
            noveltyEvo.setupEvolution(main);
            if (repository == null) {
                this.maxIterations += noveltyEvo.getMaxIterations();
            }
            for (EvolutionProgressListener l : super.listeners) {
                noveltyEvo.addProgressListener(l);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // init fitness evolution
        try {
            fitnessEvo = (FitnessNEAT) ComponentLoader.createInstance(FitnessNEAT.class, fitnessStep);
            fitnessEvo.setupEvolution(main);
            if(main.getExtraOptions().containsKey("mutationFitnessStep")) {
                NEATMutator mut = fitnessEvo.train.gaMutator();
                mut.setPPerturb(main.getExtraOptions().getDouble("mutationFitnessStep"));
            }
            this.maxIterations += fitnessEvo.getMaxIterations();
            for (EvolutionProgressListener l : listeners) {
                fitnessEvo.addProgressListener(l);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void evolutionIteration() {
        super.evolutionIteration();
        // novelty step
        if (repository == null) {
            noveltyIteration();
            if (noveltyEvo.getTrainIteration() == noveltyEvo.getMaxIterations()) {
                repository = noveltyEvo.getNoveltyRepository();
            }
            // fitness step
        } else {
            if (!fitnessStepStarted) {
                initFitnessStep();
                fitnessStepStarted = true;
            }
            fitnessIteration();
        }
    }

    protected void noveltyIteration() {
        noveltyEvo.evolutionIteration();
        if (noveltyEvo.fitnessStats.bestScore > this.bestFitness) {
            this.bestFitness = noveltyEvo.fitnessStats.bestScore;
            this.bestIndividual = decodeChromosome(noveltyEvo.fitnessStats.bestChromosome);
        }
    }

    protected void fitnessIteration() {
        fitnessEvo.evolutionIteration();
        if (fitnessEvo.fitnessStats.bestScore > this.bestFitness) {
            this.bestFitness = fitnessEvo.fitnessStats.bestScore;
            this.bestIndividual = decodeChromosome(fitnessEvo.fitnessStats.bestChromosome);
        }
    }

    protected void initFitnessStep() {
        Set<Chromosome> roots = findFitGenomes();
        double bestFit = Double.NEGATIVE_INFINITY;
        double avgFit = 0;
        for (Chromosome g : roots) {
            double f = repository.get(g).getLeft();
            bestFit = Math.max(bestFit, f);
            avgFit += f / roots.size();
        }
        notifyStatus("Found " + roots.size() + " roots");
        notifyStatus("Root Best: " + bestFit + " Avg: " + avgFit);

        Set<Chromosome> base = new HashSet<>(roots);
        for (Chromosome g : roots) {
            base.addAll(findClosest(g, closest));
        }
        bestFit = Double.NEGATIVE_INFINITY;
        avgFit = 0;
        for (Chromosome g : base) {
            double f = repository.get(g).getLeft();
            bestFit = Math.max(bestFit, f);
            avgFit += f / base.size();
        }
        notifyStatus("Expanded to " + base.size() + " by nearest neighbours");
        notifyStatus("Expanded Best: " + bestFit + " Avg: " + avgFit);

        Chromosome[] pop = generatePopulation(base);
        fitnessEvo.train.population().updatePopulation(pop);
        notifyStatus("Fitness setup completed");
    }

    protected Set<Chromosome> findFitGenomes() {
        double fitnessMin = Double.POSITIVE_INFINITY, fitnessMax = Double.NEGATIVE_INFINITY;
        for (Pair<Double, double[]> pair : repository.values()) {
            fitnessMin = Math.min(fitnessMin, pair.getLeft());
            fitnessMax = Math.max(fitnessMax, pair.getLeft());
        }
        double fitnessCutoff = fitnessMin + (fitnessMax - fitnessMin) * (1 - cutoff);
        Set<Chromosome> pretenders = new HashSet<>();
        for (Entry<Chromosome, Pair<Double, double[]>> e : repository.entrySet()) {
            if (e.getValue().getLeft() >= fitnessCutoff) {
                pretenders.add(e.getKey());
            }
        }
        return pretenders;
    }

    protected Set<Chromosome> findClosest(Chromosome pretender, int k) {
        Set<Chromosome> root = new HashSet<>(k + 1);
        root.add(pretender);

        // Measure distance to the repository
        final HashMap<Chromosome, Double> distances = new HashMap<>(repository.size());
        for (Entry<Chromosome, Pair<Double, double[]>> e : repository.entrySet()) {
            double dist = noveltyEvo.behaviourDistanceMeasure(e.getValue().getRight(), repository.get(pretender).getRight());
            distances.put(e.getKey(), dist);
        }

        // Order by similarity
        ArrayList<Chromosome> ordered = new ArrayList<>(repository.keySet());
        ordered.remove(pretender);
        Collections.sort(ordered, new Comparator<Chromosome>() {

            @Override
            public int compare(Chromosome g1, Chromosome g2) {
                return new Double(distances.get(g1)).compareTo(distances.get(g2));
            }
        });

        // Pick the K most similar
        for (int i = 0; i < k && k < ordered.size() ; i++) {
            root.add(ordered.get(i));
        }
        return root;
    }

    protected Chromosome[] generatePopulation(Set<Chromosome> root) {
        int popSize = ((NEATGADescriptor) fitnessEvo.train.descriptor()).getPopSize();
        int popIndex = 0;
        Chromosome[] pop = new Chromosome[popSize];
        ArrayList<Chromosome> rootOrdered = new ArrayList<>(root);
        Collections.sort(rootOrdered, new Comparator<Chromosome>() {

            @Override
            public int compare(Chromosome g1, Chromosome g2) {
                return Double.compare(repository.get(g2).getLeft(), repository.get(g1).getLeft());
            }
        });
        // Add the root as it is
        for (Iterator<Chromosome> it = rootOrdered.iterator(); it.hasNext() && popIndex < popSize; popIndex++) {
            Chromosome c = it.next();
            NEATChromosome clone = (NEATChromosome) fitnessEvo.train.cloneChromosome(c);
            clone.updateFitness(repository.get(c).getLeft());
            clone.setSpecieId(0);
            pop[popIndex] = clone;
        }
        // Add mutations of the root's genomes
        while (popIndex < popSize) {
            for (Iterator<Chromosome> it = rootOrdered.iterator(); it.hasNext() && popIndex < popSize; popIndex++) {
                Chromosome c = it.next();
                NEATChromosome mutated = (NEATChromosome) fitnessEvo.train.gaMutator().mutate(c);
                mutated.updateFitness(repository.get(c).getLeft());
                mutated.setSpecieId(0);
                pop[popIndex] = mutated;
            }
        }
        return pop;
    }
}
