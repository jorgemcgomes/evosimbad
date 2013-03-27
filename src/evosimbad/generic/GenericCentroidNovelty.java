/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.evolution.NoveltyNEAT;

/**
 *
 * @author jorge
 */
public class GenericCentroidNovelty extends NoveltyNEAT {

    public GenericCentroidNovelty(int numberGenerations, int k, double tInitial) {
        super(numberGenerations, k, tInitial);
    }

    @Override
    protected double behaviourDistanceMeasure(Double[] behav1, Double[] behav2) {
        // manhatan distance
        double distance = 0;
        for(int i = 0 ; i < behav1.length ; i++) {
            distance += Math.abs(behav1[i] - behav2[i]);
        }
        
        // normalize
        distance = distance / (behav1.length * 2);
        
        return distance;
    }
}
