/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

/**
 *
 * @author Jorge
 */
public interface EvolutionProgressListener {
    
    public void iterationChanged(int iteration, int max);
    
    public void evolutionStoped();
        
    public void statusMessage(String message);
    
}
