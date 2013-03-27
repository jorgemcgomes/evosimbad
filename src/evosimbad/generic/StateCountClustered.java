/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class StateCountClustered extends StateCountBrayCurtis {

    private HashMap<Chromosome, StateCount> originalEvaluation;
    private double clusterThreshold; // definir em relacao a a diferenca maxima ex: 5% diferentes
    private double effectiveThreshold;
    private HashMap<Integer, Integer> stateToCluster;
    private HashMap<Integer, Set<Integer>> clusterToStates;
    private HashMap<Integer, Double> distanceToCluster;
    private HashMap<Chromosome, StateCount> originalRepoStateCount;
    private int[] composition;
    private int bins;
    private Stats clusteredSize, newClusterDistance, clusterSize, clusterDist, clusterNeighbours, stolen;

    public StateCountClustered(int numberGenerations, int k, double tInitial, double cutoff, double clusterThreshold) {
        super(numberGenerations, k, tInitial, cutoff);
        this.clusterThreshold = clusterThreshold;
    }

    // override just to obtain some info about the evaluation function
    @Override
    protected Map<Chromosome, double[]> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        if (composition == null) {
            EvaluationFunction ef = experimentResults.values().iterator().next().get(0);
            StateCountEvaluation efProto = (StateCountEvaluation) ef;
            composition = efProto.getComposition();
            bins = efProto.getBins();
            effectiveThreshold = clusterThreshold * composition.length * (bins - 1);
        }
        return super.calculateScores(evaluations);
    }

    @Override
    public void setupEvolution(ComponentLoader main) {
        super.setupEvolution(main);
        main.getLogger().appendLine("clusterStats", "Generation", "Total Clusters",
                "Clustered size min", "Clustered size avg", "Clustered size max",
                "Cluster size min", "Cluster size avg", "Cluster size max",
                "Cluster dist min", "Cluster dist avg", "Cluster dist max",
                "New cluster dist min", "New cluster dist avg", "New cluster  dist max",
                "New cluster neighbours min", "New cluster neighbours avg", "New cluster neighbours max",
                "Stolen min", "Stolen avg", "Stolen max");
        stateToCluster = new HashMap<>(10000);
        clusterToStates = new HashMap<>(10000);
        distanceToCluster = new HashMap<>(10000);
        originalRepoStateCount = new HashMap<>();
    }

    @Override
    protected void evolutionStopped() {
        super.evolutionStopped();
        for (Integer c : clusterToStates.keySet()) {
            main.getLogger().appendLine("clusterPatterns", (Object[]) ArrayUtils.toObject(globalPatternMap.get(c)));
            Set<Integer> states = clusterToStates.get(c);
            double clusterTotal = 0;
            for(Integer s : states) {
                clusterTotal += globalCountMap.get(s);
            }
            main.getLogger().appendLine("clusterTotals", clusterTotal);
        }
        main.getLogger().flush();
    }

    @Override
    public void evolutionIteration() {
        clusteredSize = new Stats();
        newClusterDistance = new Stats();
        clusterDist = new Stats();
        clusterNeighbours = new Stats();
        stolen = new Stats();
        clusterSize = new Stats();

        super.evolutionIteration();

        for (Entry<Integer, Set<Integer>> e : clusterToStates.entrySet()) {
            clusterSize.process(null, e.getValue().size());
        }
        for (Double d : distanceToCluster.values()) {
            clusterDist.process(null, d);
        }

        main.getLogger().appendLine("clusterStats", trainIteration, clusterToStates.size(),
                clusteredSize.worstScore, clusteredSize.getAverageScore(), clusteredSize.bestScore,
                clusterSize.worstScore, clusterSize.getAverageScore(), clusterSize.bestScore,
                clusterDist.worstScore, clusterDist.getAverageScore(), clusterDist.bestScore,
                newClusterDistance.worstScore, newClusterDistance.getAverageScore(), newClusterDistance.bestScore,
                clusterNeighbours.worstScore, clusterNeighbours.getAverageScore(), clusterNeighbours.bestScore,
                stolen.worstScore, stolen.getAverageScore(), stolen.bestScore);
    }

    // NOTE: the individuals preserve their original statecount description
    // the clusters are only used when computing the distances between the statecounts
    // this is done because a state can change cluster during the evolution
    // ex: eh avaliado um SC, os estados ficam atribuidos a determinados clusters
    // depois eh avaliado o seguinte, sao adicionados novos clusters, e o mapeamento do anterior ja nao eh valido
    // HIBRIDO: sao todos transformados depois de serem chamados todos os produceBehaviourDescription
    // NECESSARIO guardar o repo com os estados originais
    @Override
    protected StateCount produceBehaviourDescription(Chromosome c, List<EvaluationFunction> evals) {
        StateCount original = super.produceBehaviourDescription(c, evals);
        // update clusters
        for (Integer i : original.getCountMap().keySet()) {
            if (!stateToCluster.containsKey(i)) {
                updateClusterMaps(i);
            }
        }
        return original;
    }

    // TODO: remind self!
    protected void updateClusterMaps(Integer newStateID) {
        byte[] newState = globalPatternMap.get(newStateID);
        // actualizar os mapas dos clusters - adicionar a um ja existente ou criar um novo cluster
        Integer closestCluster = null;
        double minDistance = Double.MAX_VALUE;
        List<Integer> neighbourClusters = new LinkedList<>();
        for (Integer cluster : clusterToStates.keySet()) {
            byte[] clusterCenter = globalPatternMap.get(cluster);
            double d = patternDistance(newState, clusterCenter);
            if (d < minDistance) {
                closestCluster = cluster;
                minDistance = d;
            }
            if (d <= effectiveThreshold * 2) {
                neighbourClusters.add(cluster);
            }
        }

        if (minDistance <= effectiveThreshold) { // adicionar a um ja existente
            stateToCluster.put(newStateID, closestCluster);
            clusterToStates.get(closestCluster).add(newStateID);
            distanceToCluster.put(newStateID, minDistance);
        } else { // criar um novo
            // criacao
            newClusterDistance.process(null, minDistance);
            clusterNeighbours.process(null, neighbourClusters.size());
            Set<Integer> newClusterElements = new HashSet<>();
            newClusterElements.add(newStateID); // add itself to the cluster
            clusterToStates.put(newStateID, newClusterElements);
            stateToCluster.put(newStateID, newStateID); // map itself to the cluster (itself)
            distanceToCluster.put(newStateID, 0d);
            // verificar se ha elementos dos clusters vizinhos tem que ser re-alocados
            for (Integer neighbour : neighbourClusters) {
                Iterator<Integer> elements = clusterToStates.get(neighbour).iterator();
                while (elements.hasNext()) {
                    Integer e = elements.next();
                    double distanceToNew = patternDistance(globalPatternMap.get(e), newState);
                    double distanceToNeighbour = distanceToCluster.get(e);
                    if (distanceToNew < distanceToNeighbour) {
                        stateToCluster.put(e, newStateID);
                        elements.remove();
                        newClusterElements.add(e);
                        distanceToCluster.put(e, distanceToNew);
                    }
                }
            }
            stolen.process(null, newClusterElements.size());
        }
    }

    @Override
    protected HashMap<Chromosome, StateCount> evaluatePopulationBehaviours(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        // the raw state count is done here -- along with the updates of the cluster structures
        // new clusters can only appear at this stage
        originalEvaluation = super.evaluatePopulationBehaviours(evaluations);

        // clustering of the current population
        HashMap<Chromosome, StateCount> newEvaluation = new HashMap<>(originalEvaluation.size() * 2);
        for (Entry<Chromosome, StateCount> e : originalEvaluation.entrySet()) {
            StateCount clustered = cluster(e.getValue());
            newEvaluation.put(e.getKey(), clustered);
            clusteredSize.process(e.getKey(), clustered.getCountMap().size());
        }

        // update clustering of repository
        for (Entry<Chromosome, Pair<Double, StateCount>> e : noveltyRepository.entrySet()) {
            StateCount clustered = cluster(originalRepoStateCount.get(e.getKey()));
            e.setValue(Pair.of(e.getValue().getLeft(), clustered));
        }

        return newEvaluation;
    }

    @Override
    protected void addToRepository(Chromosome c, double novelty) {
        super.addToRepository(c, novelty);
        // this is needed to be able to re-make the cluster attributions
        originalRepoStateCount.put(c, originalEvaluation.get(c));
    }

    protected StateCount cluster(StateCount unclusteredStateCount) {
        // fazer o mapeamento e as somas dos estados que foram agregados
        HashMap<Integer, Float> newCountMap = new HashMap<>(unclusteredStateCount.getCountMap().size());
        for (Entry<Integer, Float> state : unclusteredStateCount.getCountMap().entrySet()) {
            Integer cluster = stateToCluster.get(state.getKey());
            Float count = state.getValue();
            if (newCountMap.containsKey(cluster)) {
                newCountMap.put(cluster, newCountMap.get(cluster) + count);
            } else {
                newCountMap.put(cluster, count);
            }
        }
        return new StateCount(newCountMap, null);
    }

    protected double patternDistance(byte[] p1, byte[] p2) {
        int index = 0;
        double distance = 0;
        for (int c : composition) {
            for (int i = 0; i < c; i++) {
                distance += (double) Math.abs(p1[index] - p2[index]) / c; // normalize the sensor arrays
                index++;
            }
        }
        distance = distance / ((bins - 1) * composition.length); // normalize according to the maximum distance
        return distance;
    }
}
