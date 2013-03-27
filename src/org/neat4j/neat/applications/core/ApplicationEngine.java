package org.neat4j.neat.applications.core;

import org.neat4j.core.AIController;

/**
 * Provides a generic AIController interface for an AI application module
 * @author MSimmerson
 *
 */
public interface ApplicationEngine extends AIController {
	/**
	 * runs defined application
	 *
	 */
	public void runApplication();
}
