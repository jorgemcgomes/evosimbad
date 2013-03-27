/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

/**
 *
 * @author jorge
 */
public class StateCountHammingUPGMA extends StateCountUPGMA {

    public StateCountHammingUPGMA(int numberGenerations, int k, double tInitial, double cutoff) {
        super(numberGenerations, k, tInitial, cutoff);
    }

    @Override
    protected double patternDistance(byte[] p1, byte[] p2) {
        double dist = 0;
        for(int i = 0 ; i < p1.length ; i++) {
            if(p1[i] != p2[i]) {
                dist += 1;
            }
        }
        return dist / p1.length;
    }
}
