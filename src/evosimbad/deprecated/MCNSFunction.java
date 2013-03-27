/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated;

import evosimbad.evolution.InformedCharacterisation;

/**
 *
 * @author Jorge
 */
public abstract class MCNSFunction extends InformedCharacterisation {
    
    protected MCNSFunction(int updateRate) {
        super(updateRate);
    }

    public abstract boolean meetsMinimalCriteria();
    
    
}
