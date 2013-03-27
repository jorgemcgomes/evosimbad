/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.EvaluationFunction;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class StandardizedNoveltyNEAT extends NoveltyNEAT {

    private StandardizationResult populationStd, repositoryStd;
    private static final int switchAfterAdd = 15;
    private static final int switchIteration = 100;

    public StandardizedNoveltyNEAT(int numberGenerations, int k, double tInitial) {
        super(numberGenerations, k, tInitial);
    }

    @Override
    protected HashMap<Chromosome, Double[]> evaluatePopulationBehaviours(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        HashMap<Chromosome, Double[]> popBehavs = super.evaluatePopulationBehaviours(evaluations);

        // standardize behaviours of current population
        this.populationStd = standardize(popBehavs.values());

        // update standardization of repository
        if (noveltyRepository.size() > switchIteration) {
            this.updateRepositoryStandardization();
        }
        return popBehavs;
    }

    private void updateRepositoryStandardization() {
        ArrayList<Double[]> behavs = new ArrayList<>(noveltyRepository.size());
        for (Pair<Double, Double[]> p : noveltyRepository.values()) {
            behavs.add(p.getRight());
        }
        this.repositoryStd = standardize(behavs);
    }

    private StandardizationResult standardize(Collection<Double[]> vectors) {
        int vecLength = vectors.iterator().next().length;
        StandardizationResult res = new StandardizationResult();

        // Calculate the mean
        res.means = new Double[vecLength];
        Arrays.fill(res.means, new Double(0));
        for (Double[] b : vectors) {
            for (int i = 0; i < b.length; i++) {
                res.means[i] += b[i] / vectors.size();
            }
        }

        // Calculate the stdev
        res.stdevs = new Double[vecLength];
        Arrays.fill(res.stdevs, new Double(0));
        for (Double[] b : vectors) {
            for (int i = 0; i < b.length; i++) {
                res.stdevs[i] += Math.pow(b[i] - res.means[i], 2) / (vectors.size() - 1);
            }
        }
        for (int i = 0; i < res.stdevs.length; i++) {
            res.stdevs[i] = Math.sqrt(res.stdevs[i]);
        }

        // Standardize
        res.transformation = new HashMap<>((int) (vectors.size() * 1.5));
        for (Double[] b : vectors) {
            Double[] stdBehav = new Double[vecLength];
            for (int j = 0; j < vecLength; j++) {
                stdBehav[j] = res.stdevs[j] == 0 ? 0 : (b[j] - res.means[j]) / res.stdevs[j];
            }
            res.transformation.put(b, stdBehav);
        }

        return res;
    }

    private class StandardizationResult {

        private Double[] means;
        private Double[] stdevs;
        private Map<Double[], Double[]> transformation;
    }

    @Override
    protected List<Double> distancesToPopulation(Chromosome c) {
        Double[] b = populationBehaviours.get(c);
        return super.distancesToBehaviours(populationStd.transformation.get(b), populationStd.transformation.values());
    }

    @Override
    protected List<Double> distancesToRepository(Chromosome c) {
        if (noveltyRepository.size() < switchAfterAdd) {
            // standardize with the candidate
            ArrayList<Double[]> behavs = new ArrayList<>(noveltyRepository.size() + 1);
            Double[] behavC = populationBehaviours.get(c);
            behavs.add(behavC);
            for (Pair<Double, Double[]> p : noveltyRepository.values()) {
                behavs.add(p.getRight());
            }
            StandardizationResult standardized = standardize(behavs);
            return super.distancesToBehaviours(standardized.transformation.get(behavC), standardized.transformation.values());
        } else {
            // use the previous standardization values
            Double[] b = populationBehaviours.get(c);
            Double[] std = new Double[b.length];
            for (int i = 0; i < std.length; i++) {
                std[i] = repositoryStd.stdevs[i] == 0 ? 0 : (b[i] - repositoryStd.means[i]) / repositoryStd.stdevs[i];
            }
            return super.distancesToBehaviours(std, repositoryStd.transformation.values());
        }
    }

    @Override
    protected void addToRepository(Chromosome c, double novelty) {
        super.addToRepository(c, novelty);
        // update standardization
        if (noveltyRepository.size() >= switchAfterAdd && noveltyRepository.size() < switchIteration) {
            this.updateRepositoryStandardization();
        }
    }
}
