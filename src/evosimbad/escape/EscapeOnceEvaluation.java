/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.escape;

import evosimbad.core.EvaluationFunction;
import evosimbad.escape.EscapeAgentGenerator.EscapeAgent;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class EscapeOnceEvaluation extends EvaluationFunction {
    
    private int totalAgents = 0;
    private int escapedAgents = 0;
    
    @Override
    public void evaluate() {
        if(getCurrentEvaluation() == getTotalEvaluations()) {
            totalAgents = experiment.getEnvironment().getAgents().size();
            for(Agent ag : experiment.getEnvironment().getAgents()) {
                EscapeAgent ea = (EscapeAgent) ag;
                if(ea.getStatus() == EscapeAgent.ESCAPED) {
                    escapedAgents++;
                }
            }
        }
    }
    
    @Override
    public double getFitness() {
        return escapedAgents / (double) totalAgents;
    }
}
