/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.util.HashMap;

/**
 *
 * @author Jorge
 */
public class ExtraOptions extends HashMap<String, String> {
    
    public int getInteger(String key) {
        String get = super.get(key);
        return Integer.parseInt(get);
    }
    
    public double getDouble(String key) {
        String get = super.get(key);
        return Double.parseDouble(get);
    }
    
    public boolean getBoolean(String key) {
        String get = super.get(key);
        return Boolean.parseBoolean(get);
    }
}
