/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.mix;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import java.util.List;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author Jorge
 */
public class MinimalCriteriaNoveltyNEAT extends NoveltyNEAT {

    private PMCNS pmcns;
    
    public MinimalCriteriaNoveltyNEAT(int numberGenerations, int k, double tInitial, double percentile, double adjust) {
        super(numberGenerations, k, tInitial);
        pmcns = new PMCNS(percentile, adjust);
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        pmcns.setComponentLoader(main);
    }

    @Override
    protected Map<Chromosome, Double> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        Map<Chromosome, Double> noveltyScores = super.calculateScores(evaluations);
        return pmcns.score(noveltyScores, fitnessScores);
    }
}
