/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation.test;

import evosimbad.core.EvaluationFunction;

/**
 *
 * @author Jorge
 */
public class CenterMassFinal extends EvaluationFunction {

    private int evaluatedSteps;
    private double total = 0;
    private int count = 0;

    public CenterMassFinal(int updateRate, int evaluatedSteps) {
        super(updateRate);
        this.evaluatedSteps = evaluatedSteps;
    }

    @Override
    public void evaluate() {
        if (experiment.getCurrentStep() >= experiment.getTotalSteps() - evaluatedSteps) {
            total += CenterMassEvaluation.averageDistanceToCenterMass(
                    experiment.getEnvironment().getAgents());
            count++;
        }
    }

    @Override
    public double getFitness() {
        return -total / count;
    }
}
