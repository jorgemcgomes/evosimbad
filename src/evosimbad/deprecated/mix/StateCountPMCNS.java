/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.mix;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.evolution.PMCNS;
import java.util.List;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class StateCountPMCNS extends StateCountNovelty {

    private PMCNS pmcns;
    
    public StateCountPMCNS(int numberGenerations, int k, double tInitial, double percentile, double adjust, double cutoff) {
        super(numberGenerations, k, tInitial, cutoff);
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
