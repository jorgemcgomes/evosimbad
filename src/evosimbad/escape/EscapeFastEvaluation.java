/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.escape;

import evosimbad.core.EvaluationFunction;
import evosimbad.escape.EscapeEnvironment.EscapeEnv;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class EscapeFastEvaluation extends EvaluationFunction {

    private int totalAgents = 0;
    private int escapedAgents = 0;
    private int closeTime = 0;

    @Override
    public void evaluate() {
        EscapeEnv env = (EscapeEnv) experiment.getEnvironment();
        if (closeTime == 0 && env.getDoor().isClosed()) {
            closeTime = getCurrentEvaluation();
        }

        if (getCurrentEvaluation() == getTotalEvaluations()) {
            totalAgents = experiment.getEnvironment().getAgents().size();
            for (Agent ag : experiment.getEnvironment().getAgents()) {
                EscapeAgentGenerator.EscapeAgent ea = (EscapeAgentGenerator.EscapeAgent) ag;
                if (ea.getStatus() == EscapeAgentGenerator.EscapeAgent.ESCAPED) {
                    escapedAgents++;
                }
            }
            if (closeTime == 0) {
                closeTime = getTotalEvaluations();
            }
        }

    }

    @Override
    public double getFitness() {
        // escape more is always better than escape faster
        // escape n at max speed = escape n-1 at slowest speed
        double escapeSpeed = 1 - (double) closeTime / getTotalEvaluations();
        double escapeAmount = (escapedAgents - 1.0) / (totalAgents - 1.0);
        double weight = 1.0 / (totalAgents);
        return escapedAgents > 0 ? escapeSpeed * weight + (escapeAmount) * (1 - weight) : 0;
    }
}
