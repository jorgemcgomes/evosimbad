/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.ComponentLoader;
import java.util.Map;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public abstract class NoveltyFitnessMixer {

    protected ComponentLoader main;

    public void init(ComponentLoader main) {
        this.main = main;
    }
    
    protected abstract Map<Chromosome, double[]> mix(Map<Chromosome, Double> noveltyScores, Map<Chromosome, Double> fitnessScores);
}
