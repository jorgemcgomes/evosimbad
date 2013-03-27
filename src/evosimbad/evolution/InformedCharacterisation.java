/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.EvaluationFunction;

/**
 *
 * @author jorge
 */
public abstract class InformedCharacterisation extends EvaluationFunction {

    protected InformedCharacterisation(int updateRate) {
        super(updateRate);
    }

    public abstract double[] getBehaviour();
}
