/*
 * Created on Sep 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.core;

public interface AIController {
	/**
	 * Initialises the controller environment described by config
	 * @param config
	 * @throws InitialisationFailedException if environment creation fails
	 */
	public void initialise(AIConfig config) throws InitialisationFailedException ;
}
