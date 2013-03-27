/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.mo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.neat4j.neat.core.NEATChromosome;
import org.neat4j.neat.core.NEATGADescriptor;
import org.neat4j.neat.core.NEATGeneticAlgorithm;
import org.neat4j.neat.core.NEATLinkGene;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.core.NEATNodeGene;
import org.neat4j.neat.core.NEATSpecieManager;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.ChromosomeSet;
import org.neat4j.neat.ga.core.Gene;

/**
 *
 * @author jorge
 */
public class MultiObjectiveNEAT extends NEATGeneticAlgorithm {

    protected List<Individual> parentPopulationP = null;
    protected List<Individual> childPopulationQ = new ArrayList<>(1000);
    private boolean diversityObjective;
    private boolean objectivesNaturalOrder; // is lower better? TRUE - YES

    public MultiObjectiveNEAT(NEATGADescriptor descriptor, boolean diversityObjective) {
        super(descriptor);
        this.diversityObjective = diversityObjective;
        objectivesNaturalOrder = descriptor.isNaturalOrder();
        descriptor.setNaturalOrder(true); // NSGA-II requires this
    }

    @Override
    protected void evaluatePopulation(Chromosome[] genoTypes) {
        if (!(gaEvaluator() instanceof MultiObjectiveFitnessFunction)) {
            System.out.println("ERROR: Need multi objective fitness function");
            return;
        }
        
        
        double excessCoeff = ((NEATGADescriptor) descriptor()).getExcessCoeff();
        double disjointCoeff = ((NEATGADescriptor) descriptor()).getDisjointCoeff();
        double weightCoeff = ((NEATGADescriptor) descriptor()).getWeightCoeff();

        MultiObjectiveFitnessFunction f = (MultiObjectiveFitnessFunction) gaEvaluator();
        childPopulationQ.clear();
        for (Chromosome c : genoTypes) {
            double[] eval = f.evaluateObjectives(c);
            if (diversityObjective) {
                double div = diversityMeasure(c, genoTypes, excessCoeff, disjointCoeff, weightCoeff);
                eval = ArrayUtils.add(eval, div);
            }
            Individual i = new Individual(c);
            i.evaluation = eval;
            childPopulationQ.add(i);
        }
    }

    protected double diversityMeasure(Chromosome c, Chromosome[] pop, double excessCoeff, double disjointCoeff, double weightCoeff) {
        double avgDistance = 0;
        for (Chromosome p : pop) {
            if (c != p) {
                avgDistance += NEATSpecieManager.specieManager().compatibilityScore(
                        c, p, excessCoeff, disjointCoeff, weightCoeff);
            }
        }
        return avgDistance / (pop.length - 1);
    }

    @Override
    public void runEvolutionCycle(Chromosome[] currentGen) {
        if (parentPopulationP == null) { // first generation
            // Each solution is a assigned a fitness equal to its non-domination level
            List<List<Individual>> fronts = fastNondominatedSort(childPopulationQ);
            for (List<Individual> front : fronts) {
                for (Individual i : front) {
                    i.chromosome.updateFitness(i.rank);
                }
            }
            Chromosome[] offspring = spawn(childPopulationQ);
            super.population().updatePopulation(offspring);
            parentPopulationP = new ArrayList<>(childPopulationQ);
        } else {
            List<Individual> combinedPopulationR = new ArrayList<>();
            combinedPopulationR.addAll(parentPopulationP);
            combinedPopulationR.addAll(childPopulationQ);

            List<List<Individual>> fronts = fastNondominatedSort(combinedPopulationR);
            List<Individual> matingPool = new ArrayList<>(currentGen.length);
            int i = 0;
            while (matingPool.size() < currentGen.length) {
                crowdingDistanceAssignement(fronts.get(i));
                matingPool.addAll(fronts.get(i));
                i++;
            }
            // sort in descending order using >=n
            Collections.sort(matingPool, new CrowdedComparisonOperator()); // ascending order
            Collections.reverse(matingPool); // descending order

            // choose the first N elements of Pt+1
            while (matingPool.size() > currentGen.length) {
                matingPool.remove(currentGen.length);
            }
            // assign fitness score according to the order - needed for tournament selection
            for (i = 0; i < matingPool.size(); i++) {
                matingPool.get(i).chromosome.updateFitness(i);
            }

            // use selection, crossover and mutation to create new population
            Chromosome[] offspring = spawn(matingPool);
            parentPopulationP = new ArrayList<>(matingPool);
            super.population().updatePopulation(offspring); // childPopulation vai ser preenchida na proxima avaliacao
        }
    }

    protected boolean dominates(Individual i1, Individual i2) {
        boolean oneBetter = false;
        for (int i = 0; i < i1.evaluation.length; i++) {
            if ((objectivesNaturalOrder && i1.evaluation[i] > i2.evaluation[i])
                    || (!objectivesNaturalOrder && i1.evaluation[i] < i2.evaluation[i])) {
                return false;
            } else if ((objectivesNaturalOrder && i1.evaluation[i] < i2.evaluation[i])
                    || (!objectivesNaturalOrder && i1.evaluation[i] > i2.evaluation[i])) {
                oneBetter = true;
            }
        }
        return oneBetter;
    }

    /*
     * Returns the fronts and assign the nondomination rank to each individual
     */
    protected List<List<Individual>> fastNondominatedSort(List<Individual> inds) {
        // init
        for (Individual i : inds) {
            i.S = new ArrayList<>();
            i.dominatedBy = new ArrayList<>();
            i.n = 0;
        }

        List<List<Individual>> F = new ArrayList<>();
        List<Individual> F1 = new ArrayList<>();
        for (Individual p : inds) {
            for (Individual q : inds) {
                if (p != q) {
                    if (dominates(p, q)) {
                        p.S.add(q);
                    } else if (dominates(q, p)) {
                        p.n = p.n + 1;
                        p.dominatedBy.add(q);
                    }
                }
            }
            //System.out.println("dom by: " + p.dominatedBy.size() + " | doms: " + p.S.size());
            if (p.n == 0) {
                F1.add(p);
            }
        }
        F.add(F1);
        int i = 0;
        while (!F.get(i).isEmpty()) {
            List<Individual> H = new ArrayList<>();
            for (Individual p : F.get(i)) {
                for (Individual q : p.S) {
                    q.n = q.n - 1;
                    if (q.n == 0) {
                        H.add(q);
                    }
                }
            }
            i = i + 1;
            F.add(H);
        }

        // assign ranks
        for (i = 0; i < F.size(); i++) {
            for (Individual ind : F.get(i)) {
                ind.rank = i + 1;
            }
        }
        return F;
    }

    protected void crowdingDistanceAssignement(List<Individual> I) {
        int mTotal = I.get(0).evaluation.length;
        int l = I.size();
        for (Individual i : I) {
            i.crowdingDistance = 0;
        }
        for (int m = 0; m < mTotal; m++) {
            final int mm = m;
            Collections.sort(I, new Comparator<Individual>() {
                @Override
                public int compare(Individual ind1, Individual ind2) {
                    return Double.compare(ind1.evaluation[mm], ind2.evaluation[mm]);
                }
            });
            I.get(0).crowdingDistance = Double.POSITIVE_INFINITY;
            I.get(l - 1).crowdingDistance = Double.POSITIVE_INFINITY;
            for (int i = 1; i < l - 1; i++) {
                I.get(i).crowdingDistance = I.get(i).crowdingDistance
                        + (I.get(i + 1).evaluation[m] - I.get(i - 1).evaluation[m]);
            }
        }
    }

    class CrowdedComparisonOperator implements Comparator<Individual> {

        @Override
        public int compare(Individual i, Individual j) {
            if (i.rank < j.rank || (i.rank == j.rank && i.crowdingDistance > j.crowdingDistance)) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    protected Chromosome[] spawn(List<Individual> pool) {
        Chromosome[] offspring = new Chromosome[super.descriptor().gaPopulationSize()];
        Chromosome[] parentsPool = new Chromosome[pool.size()];
        for (int i = 0; i < parentsPool.length; i++) {
            parentsPool[i] = pool.get(i).chromosome;
        }
        for (int i = 0; i < offspring.length; i++) {
            ChromosomeSet parents = gaSelector().selectParents(parentsPool, false);
            ChromosomeSet child = gaCrossover().crossOver(this.cloneParents(parents));
            offspring[i] = gaMutator().mutate(child.nextChromosome());

            // debug from now on
            Chromosome p1 = (Chromosome) parents.get(0);
            Chromosome p2 = (Chromosome) parents.get(1);
            Chromosome c = offspring[i];
            /*if (c.genes().length < p1.genes().length && c.genes().length < p2.genes().length) {
             System.out.println("Gene count error: " + c.genes().length + " ; " + p1.genes().length + " ; " + p2.genes().length);
             }
             int lp1 = countLinks(p1), lp2 = countLinks(p2), lc = countLinks(c);
             if (lc < lp1 && lc < lp2) {
             System.out.println("Link count error: " + lc + " ; " + lp1 + " ; " + lp2);
             }*/
            int lp1 = countLinks(p1);
            int lp2 = countLinks(p2);
            int lc = countLinks(c);

            int lp1All = countLinksAll(p1);
            int lp2All = countLinksAll(p2);
            int lcAll = countLinksAll(c);

            /*if(lp1 != lp1All || lp2 != lp2All) {
             System.out.println("Has disabled");
             }*/

            if (lc < lp1 && lc < lp2) {
                System.out.println("Enabled - P1: " + lp1 + " ; P2: " + lp2 + " ; C: " + lc);
                System.out.println("All     - P1: " + lp1All + " ; P2: " + lp2All + " ; C: " + lcAll);
            }
        }
        return offspring;
    }

    private int countLinks(Chromosome c) {
        int count = 0;
        for (Gene g : c.genes()) {
            if (g instanceof NEATLinkGene && ((NEATLinkGene) g).isEnabled()) {
                count++;
            }
        }
        return count;
    }

    private int countLinksAll(Chromosome c) {
        int count = 0;
        for (Gene g : c.genes()) {
            if (g instanceof NEATLinkGene) {
                count++;
            }
        }
        return count;
    }

    // debug
    /*private int countLinks2(Chromosome c) { // here
     NEATNetDescriptor descr = new NEATNetDescriptor(0, null);
     descr.updateStructure(c);
     NEATNeuralNet net = new NEATNeuralNet();
     net.createNetStructure(descr);
     net.updateNetStructure();
     int links = 0;
     for (NEATNeuron neuron : net.neurons()) {
     links += neuron.incomingSynapses().size();
     }
     return links;
     }*/
    private ChromosomeSet cloneParents(ChromosomeSet clonee) {
        ChromosomeSet clonedSet = new ChromosomeSet();

        Chromosome cloneeChromo = clonee.nextChromosome();
        Chromosome clone = clone(cloneeChromo);
        clone.updateFitness(cloneeChromo.fitness());
        clonedSet.add(clone);

        cloneeChromo = clonee.nextChromosome();
        clone = clone(cloneeChromo);
        clone.updateFitness(cloneeChromo.fitness());
        clonedSet.add(clone);

        return (clonedSet);
    }

    public Chromosome clone(Chromosome c) {
        Gene[] genesClone = new Gene[c.genes().length];
        int i = 0;
        for (Gene g : c.genes()) {
            if(g instanceof NEATLinkGene) {
                NEATLinkGene link = (NEATLinkGene) g;
                NEATLinkGene newLink = new NEATLinkGene(link.getInnovationNumber(), link.isEnabled(), link.getFromId(), link.getToId(), link.getWeight());
                newLink.setRecurrent(link.isRecurrent());
                newLink.setSelfRecurrent(link.isSelfRecurrent());
                genesClone[i++] = newLink;
            } else {
                NEATNodeGene node = (NEATNodeGene) g;
                NEATNodeGene newNode = new NEATNodeGene(node.getInnovationNumber(), node.id(), node.sigmoidFactor(), node.getType(), node.bias());
                genesClone[i++] = newNode;
            }
        }

        Chromosome newC = new NEATChromosome(genesClone);
        // mod end

        newC.updateFitness(c.fitness());

        return (newC);
    }

    public class Individual {

        Individual(Chromosome c) {
            this.chromosome = c;
        }
        double[] evaluation;
        Chromosome chromosome;
        int rank;
        double crowdingDistance;
        int n;
        List<Individual> S;
        List<Individual> dominatedBy;

        public double[] getEvaluation() {
            return evaluation;
        }

        public Chromosome getChromosome() {
            return chromosome;
        }

        public int getRank() {
            return rank;
        }

        public double getCrowdingDistance() {
            return crowdingDistance;
        }

        public int getN() {
            return n;
        }

        public List<Individual> getS() {
            return S;
        }

        public List<Individual> getDominatedBy() {
            return dominatedBy;
        }
    }
}
