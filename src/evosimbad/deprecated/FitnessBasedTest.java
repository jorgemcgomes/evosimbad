/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.evolution.FitnessNEAT;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class FitnessBasedTest extends FitnessNEAT {

    private Map<Integer, Float> globalMap = new LinkedHashMap<>();
    private Map<Integer, byte[]> key = new HashMap<>();
    private float cutoff;

    public FitnessBasedTest(int numberGenerations, float cutoff) {
        super(numberGenerations);
        this.cutoff = cutoff;
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        main.getLogger().appendLine("genericMapStats",
                "Generation",
                "Most patterns",
                "Avg patterns",
                "Least patterns",
                "Most discarded",
                "Average discarded",
                "Least discarded",
                "Patterns so far");
    }

    @Override
    protected Map<Chromosome, Double> calculateFitnessScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        System.gc();
        Stats mapSizeStats = new Stats();
        Stats filterStats = new Stats();
        for (List<EvaluationFunction> eval : evaluations.values()) {
            Map<Integer, Float> countMap = new HashMap<>();
            Map<Integer, byte[]> keyMap = new HashMap<>();
            for (EvaluationFunction ef : eval) {
                StateCountEvaluation sc = (StateCountEvaluation) ef;
                mergeCountMap(countMap, sc.getStateCount().getCountMap());
                keyMap.putAll(sc.getStateCount().getPatternMap());
            }
            
            int originalSize = countMap.size();
            
            // filter map
            float total = 0;
            for(Float v : countMap.values()) {
                total += v;
            }   
            
            //System.out.println("tot: " + total);
            
            // sort by frequency - from most frequent to least frequent
            /*ArrayList<Entry<Integer, Float>> countArray = new ArrayList<>(countMap.entrySet());
            Collections.sort(countArray, new Comparator<Entry<Integer, Float>>() {
                @Override
                public int compare(Entry<Integer, Float> e1, Entry<Integer, Float> e2) {
                    return -Float.compare(e1.getValue(), e2.getValue());
                }
            });
            
            // apply cutoff
            float threshold = total * cutoff;
            float accum = 0;
            for(Entry<Integer, Float> e : countArray) {
                if(accum > threshold) {
                    countMap.remove(e.getKey());
                    keyMap.remove(e.getKey());
                } else {
                    accum += e.getValue();
                }
            }*/
            
            ArrayList<Entry<Integer, Float>> list = new ArrayList<>(countMap.entrySet());
            float threshold = total * cutoff;
            //System.out.println("thr: " + threshold);
            for(Entry<Integer, Float> e : list) {
                if(e.getValue() < threshold) {
                    countMap.remove(e.getKey());
                    keyMap.remove(e.getKey());                    
                }
            }
            
            filterStats.process(null, originalSize - countMap.size());
            
            // integrate in global maps
            mergeCountMap(globalMap, countMap);
            key.putAll(keyMap);
            
            // numero de padroes descobertos em cada individuo
            mapSizeStats.process(null, countMap.size());
        }

        main.getLogger().appendLine("genericMapStats",
                trainIteration,
                mapSizeStats.bestScore,
                mapSizeStats.getAverageScore(),
                mapSizeStats.worstScore,
                filterStats.bestScore,
                filterStats.getAverageScore(),
                filterStats.worstScore,
                globalMap.size());

        System.out.println("key:   " + key.size());
        System.out.println("count: " + globalMap.size());
        
        return super.calculateFitnessScores(evaluations);
    }

    @Override
    protected void evolutionStopped() {
        super.evolutionStopped();

        // totais dos padroes
        for (Integer l : globalMap.keySet()) {
            main.getLogger().appendLine("genericTotals", l, globalMap.get(l)); 
            main.getLogger().appendLine("patterns", (Object[]) ArrayUtils.toObject(key.get(l)));
        }
        main.getLogger().flush();
    }

    static void mergeCountMap(Map<Integer, Float> map, Map<Integer, Float> other) {
        for (Entry<Integer, Float> e : other.entrySet()) {
            if (!map.containsKey(e.getKey())) {
                map.put(e.getKey(), e.getValue());
            } else {
                map.put(e.getKey(), map.get(e.getKey()) + e.getValue());
            }
        }
    }
}
