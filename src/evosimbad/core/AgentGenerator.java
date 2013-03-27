/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Jorge
 */
public interface AgentGenerator extends Serializable {
    
    public Set<NNAgent> generateAgents();
      
    public int getInputs();
        
    public int getOutputs();
}
