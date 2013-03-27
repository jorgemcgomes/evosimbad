/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.io.Serializable;
import simbad.sim.EnvironmentDescription;

/**
 *
 * @author Jorge
 */
public interface EnvironmentGenerator extends Serializable {
    
    public EnvironmentDescription generateEnvironment();

}
