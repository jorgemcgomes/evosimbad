/*
 * Created on Sep 29, 2004
 *
 */
package org.neat4j.neat.core;

import java.util.HashMap;

import org.neat4j.core.AIConfig;

/**
 * Holds an object representation of the NEAT config
 * @author MSimmerson
 *
 */
public class NEATConfig implements AIConfig {
	private HashMap config;
	
	public NEATConfig(HashMap config) {
		this.config = new HashMap(config);
	}	
		
	// default
	public NEATConfig() {
		this.config = new HashMap();
	}
	/**
	 * @see org.neat4j.ailibrary.core.AIConfig#configElement(java.lang.String)
	 */
	public String configElement(String elementKey) {
		return ((String)this.config.get(elementKey));
	}

	public void updateConfig(String elementKey, String elementValue) {
		this.config.put(elementKey, elementValue);
	}
}
