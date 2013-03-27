/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.generic;

import evosimbad.core.EvaluationFunction;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class StateCountUPGMA extends StateCountNovelty {

    private int[] composition;
    private int bins;

    public StateCountUPGMA(int numberGenerations, int k, double tInitial, double cutoff) {
        super(numberGenerations, k, tInitial, cutoff);
    }

    @Override
    protected Map<Chromosome, double[]> calculateScores(Map<Chromosome, List<EvaluationFunction>> evaluations) {
        if (composition == null) {
            EvaluationFunction ef = experimentResults.values().iterator().next().get(0);
            StateCountEvaluation efProto = (StateCountEvaluation) ef;
            composition = efProto.getComposition();
            bins = efProto.getBins();
        }
        return super.calculateScores(evaluations);
    }
    
    // optimizacao: guardar resultados intermedios -- sera que compensa?
    // teriam que ser geridas e acedidas estruturas de dados algo complexas, enquanto que o calculo
    // da distancia entre padroes e bastante leve
    @Override
    protected double behaviourDistanceMeasure(StateCount sc1, StateCount sc2) {
        // calculate the totals
        double total1 = 0, total2 = 0;
        for (Float f : sc1.getCountMap().values()) {
            total1 += f;
        }
        for (Float f : sc2.getCountMap().values()) {
            total2 += f;
        }

        // calculate UPGMA
        double accum = 0;
        for (Entry<Integer, Float> e1 : sc1.getCountMap().entrySet()) {
            for (Entry<Integer, Float> e2 : sc2.getCountMap().entrySet()) {
                double dist = patternDistance(globalPatternMap.get(e1.getKey()), globalPatternMap.get(e2.getKey()));
                dist = dist * e1.getValue() * e2.getValue();
                accum += dist;
            }
        }
        accum = accum / (total1 * total2);

        return accum;
    }
    
    /*
     * This can be further optimized
     * Buffer: Obter uma hash do conjunto p1 + p2 (a hash deve ser igual a p2 + p1)
     * guardar as distâncias dos padrões a cada geração
     * ou manter buffer de tamanho fixo em que as coisas vao entrando e saindo conforme deixam de ser usadas
     * Million dolar question: Calcular o hash sera mais eficaz que calcular a distancia entre os padroes??
     * 
     * 
     * Distance normalised between 0 and 1
     */
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
