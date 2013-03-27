/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author jorge
 */
public class StateCount implements Serializable {

    private HashMap<Integer, Float> countMap;
    private HashMap<Integer, byte[]> patternMap;

    public StateCount(HashMap<Integer, Float> countMap, HashMap<Integer, byte[]> patternMap) {
        this.countMap = countMap;
        this.patternMap = patternMap;
    }

    public HashMap<Integer, Float> getCountMap() {
        return countMap;
    }

    public HashMap<Integer, byte[]> getPatternMap() {
        return patternMap;
    }  

    public void setCountMap(HashMap<Integer, Float> countMap) {
        this.countMap = countMap;
    }

    public void setPatternMap(HashMap<Integer, byte[]> patternMap) {
        this.patternMap = patternMap;
    }
    
    
}
