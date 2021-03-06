package org.neat4j.neat.ga.core;

/**
 * @author MSimmerson
 *
 */
public interface FitnessFunction extends Operator {
	public double evaluate(Chromosome genoType);
	public int requiredChromosomeSize();
}
