/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.mix;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class RandomPMCNS extends NEATEvolution {

    private PMCNS pmcns;

    public RandomPMCNS(int numberGenerations, double percentile, double adjust) {
        super(numberGenerations);
        pmcns = new PMCNS(percentile, adjust);
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        pmcns.setComponentLoader(main);
    }

    @Override
    protected Map<Chromosome, Double> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        Map<Chromosome, Double> randomScores = new HashMap<>();
        for(Chromosome c : evaluations.keySet()) {
            randomScores.put(c, Math.random());
        }
        return pmcns.score(randomScores, fitnessScores);
    }
}
