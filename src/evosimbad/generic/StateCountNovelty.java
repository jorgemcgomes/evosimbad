/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.evolution.BaseNoveltyImplementation;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class StateCountNovelty extends BaseNoveltyImplementation<StateCount> {

    private double cutoff;
    // the following are just used for logs
    protected Map<Integer, Float> globalCountMap = new LinkedHashMap<>(10000, 0.25f);
    protected Map<Integer, byte[]> globalPatternMap = new HashMap<>(100000, 0.25f);
    protected Stats mapSizeStats, filterStats, commonPatternStats, uniquePatternStats, commonRatioStats;

    public StateCountNovelty(int numberGenerations, int k, double tInitial, double cutoff) {
        super(numberGenerations, k, tInitial);
        this.cutoff = cutoff;
    }

    /*
     * Just for logs
     */
    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        main.getLogger().appendLine("stateCountStats",
                "Generation",
                "Most patterns",
                "Avg patterns",
                "Least patterns",
                "Most nonfilter",
                "Avg nonfilter",
                "Least nonfilter",
                "Patterns so far",
                "Most common patterns",
                "Avg common patterns",
                "Least common patterns",
                "Most unique patterns",
                "Avg unique patterns",
                "Least unique patterns",
                "Avg common ratio");
    }

    /*
     * Just for logs
     */
    @Override
    protected Map<Chromosome, double[]> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        mapSizeStats = new Stats();
        filterStats = new Stats();
        commonPatternStats = new Stats();
        uniquePatternStats = new Stats();
        commonRatioStats = new Stats();
        Map<Chromosome, double[]> s = super.calculateScores(evaluations);
        main.getLogger().appendLine("stateCountStats",
                trainIteration,
                mapSizeStats.bestScore,
                mapSizeStats.getAverageScore(),
                mapSizeStats.worstScore,
                filterStats.bestScore,
                filterStats.getAverageScore(),
                filterStats.worstScore,
                globalCountMap.size(),
                commonPatternStats.bestScore,
                commonPatternStats.getAverageScore(),
                commonPatternStats.worstScore,
                uniquePatternStats.bestScore,
                uniquePatternStats.getAverageScore(),
                uniquePatternStats.worstScore,
                commonRatioStats.getAverageScore());

        return s;
    }

    @Override
    protected StateCount produceBehaviourDescription(Chromosome c, List<EvaluationFunction> evals) {
        StateCount merged = merge(evals);
        int originalSize = merged.getCountMap().size();
        filter(merged);

        // logs
        filterStats.process(c, originalSize);
        mapSizeStats.process(c, merged.getCountMap().size());
        mergeCountMap(globalCountMap, merged.getCountMap());
        globalPatternMap.putAll(merged.getPatternMap());
        
        merged.setPatternMap(null); // clear patterns -- they are already stored in the global map
        
        return merged;
    }

    protected StateCount merge(List<EvaluationFunction> evals) {
        HashMap<Integer, Float> countMap = new HashMap<>(500);
        HashMap<Integer, byte[]> patternMap = new HashMap<>(500);
        // sum all maps
        for (EvaluationFunction ef : evals) {
            StateCount sc = ((StateCountEvaluation) ef).getStateCount();
            mergeCountMap(countMap, sc.getCountMap());
            if (sc.getPatternMap() != null) {
                patternMap.putAll(sc.getPatternMap());
            }
        }
        // divide each count by the number of trials
        for (Entry<Integer, Float> e : countMap.entrySet()) {
            e.setValue(e.getValue() / evals.size());
        }
        return new StateCount(countMap, patternMap);
    }

    protected void mergeCountMap(Map<Integer, Float> map, Map<Integer, Float> other) {
        for (Map.Entry<Integer, Float> e : other.entrySet()) {
            if (!map.containsKey(e.getKey())) {
                map.put(e.getKey(), e.getValue());
            } else {
                map.put(e.getKey(), map.get(e.getKey()) + e.getValue());
            }
        }
    }

    protected void filter(StateCount sc) {
        double total = 0;
        for (Float v : sc.getCountMap().values()) {
            total += v;
        }
        double t = total * cutoff;
        Iterator<Entry<Integer, Float>> it = sc.getCountMap().entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, Float> next = it.next();
            if (next.getValue() < t) {
                it.remove();
            }
        }
    }

    /*
     * Just for logs
     */
    @Override
    protected void evolutionStopped() {
        super.evolutionStopped();
        for (Integer i : globalCountMap.keySet()) {
            main.getLogger().appendLine("genericTotals", i, globalCountMap.get(i));
            main.getLogger().appendLine("patterns", (Object[]) ArrayUtils.toObject(globalPatternMap.get(i)));
        }
        main.getLogger().flush();
    }

    /*
     * This must be as efficient as possible
     */
    @Override
    protected double behaviourDistanceMeasure(StateCount sc1, StateCount sc2) {
        HashMap<Integer, Float> m1 = sc1.getCountMap();
        HashMap<Integer, Float> m2 = sc2.getCountMap();

        int commonCount = 0, uniqueCount = 0;
        double dist = 0;
        for (Integer i : m1.keySet()) {
            if (m2.containsKey(i)) { // os que estao em ambos
                dist += Math.pow(m1.get(i) - m2.get(i), 2);
                commonCount++;
            } else { // os que estao apenas em m1
                dist += Math.pow(m1.get(i), 2);
                uniqueCount++;
            }
        }
        for (Integer i : m2.keySet()) {
            if (!m1.containsKey(i)) { // nao estao em m1 - logo estao apenas em m2
                dist += Math.pow(m2.get(i), 2);
                uniqueCount++;
            }
        }
        commonPatternStats.process(null, commonCount);
        uniquePatternStats.process(null, uniqueCount);
        commonRatioStats.process(null, (double) commonCount / Math.min(m1.size(), m2.size()));

        return Math.sqrt(dist);

        // encontrar comuns e calcular dist euclidiana
        // encontrar unicos de m1 e calcular distancia euclidiana para vector de zeros
        // fazer o mesmo para m2
        // combinar as varias distancias - somar diferencas dos quadrados e fazer a raiz
        // Alternativa: construir vectores e calcular distancia entre eles
    }
}
