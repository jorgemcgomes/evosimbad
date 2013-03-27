/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import evosimbad.core.AgentGenerator;
import evosimbad.core.NNAgent;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jorge
 */
public abstract class FixedAgentGenerator implements AgentGenerator {

    protected int numberOfAgents;
    protected String commonName;
    
    public FixedAgentGenerator(int count, String commonName) {
        this.numberOfAgents = count;
        this.commonName = commonName;
    }
    
    @Override
    public Set<NNAgent> generateAgents() {
        HashSet<NNAgent> ags = new HashSet<>(numberOfAgents);
        for(int i = 0 ; i < numberOfAgents ; i++) {
            NNAgent ag = generateAgent(commonName + i);
            ags.add(ag);
        }
        return ags;
    }
    
    public abstract NNAgent generateAgent(String suggestedName);
}
