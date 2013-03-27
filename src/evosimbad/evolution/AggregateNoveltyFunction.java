/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

/**
 *
 * @author jorge
 */
public abstract class AggregateNoveltyFunction extends InformedCharacterisation {

    public AggregateNoveltyFunction(int updateRate) {
        super(updateRate);
    }

    public abstract int[] splitIndexes();
}
