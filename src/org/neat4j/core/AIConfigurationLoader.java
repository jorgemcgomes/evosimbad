/*
 * Created on Oct 12, 2004
 *
 */
package org.neat4j.core;


/**
 * @author MSimmerson
 *
 */
public interface AIConfigurationLoader {
	/**
	 * Loads a configuration file from location, and creates an AIConfig object
	 * that holds the full configuration
	 * @param location - file location
	 * @return AIConfig that holds the full configuration
	 */
	public AIConfig loadConfig(String location);
}
