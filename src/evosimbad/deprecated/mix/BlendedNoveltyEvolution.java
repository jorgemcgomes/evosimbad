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
public class BlendedNoveltyEvolution extends NoveltyNEAT {
  
    private LinearBlend blend;
    
    public BlendedNoveltyEvolution(int numberGenerations, int k, double tInitial, double noveltyRatio) {
        super(numberGenerations, k, tInitial);
        blend= new LinearBlend(noveltyRatio);
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        blend.setComponentLoader(main);
    }

    @Override
    protected Map<Chromosome, Double> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        Map<Chromosome, Double> noveltyScores = super.calculateScores(evaluations);
        return blend.score(noveltyScores, fitnessScores);
    }
}
