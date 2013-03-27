/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.evolution;

import evosimbad.core.ComponentLoader;
import evosimbad.core.EvaluationFunction;
import evosimbad.core.Simulation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Jorge
 */
public class CompositeNoveltyFunction extends AggregateNoveltyFunction {

    private static Pair<Constructor, Object[]> fitConstructor;
    private static ArrayList<Pair<Constructor, Object[]>> novConstructors;
    private InformedCharacterisation[] noveltyFunctions;
    private int[] splits;
    private EvaluationFunction fitnessFunction;

    public CompositeNoveltyFunction(String fit, String nov1, String nov2, String nov3, String nov4) {
        super(1);
        try {
            initConstructors(fit, nov1, nov2, nov3, nov4);
            fitnessFunction = (EvaluationFunction) fitConstructor.getLeft().newInstance(fitConstructor.getRight());
            noveltyFunctions = new InformedCharacterisation[novConstructors.size()];
            for (int i = 0; i < noveltyFunctions.length; i++) {
                noveltyFunctions[i] = (InformedCharacterisation) novConstructors.get(i).getLeft().newInstance(novConstructors.get(i).getRight());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static synchronized void initConstructors(String fit, String nov1, String nov2, String nov3, String nov4) throws Exception {
        if (fitConstructor == null || novConstructors == null) {
            fitConstructor = ComponentLoader.findConstructor(EvaluationFunction.class, fit);
            novConstructors = new ArrayList<>(4);
            if (nov1 != null) {
                novConstructors.add(ComponentLoader.findConstructor(InformedCharacterisation.class, nov1));
            }
            if (nov2 != null) {
                novConstructors.add(ComponentLoader.findConstructor(InformedCharacterisation.class, nov2));
            }
            if (nov3 != null) {
                novConstructors.add(ComponentLoader.findConstructor(InformedCharacterisation.class, nov3));
            }
            if (nov4 != null) {
                novConstructors.add(ComponentLoader.findConstructor(InformedCharacterisation.class, nov4));
            }
            if (novConstructors.isEmpty()) {
                throw new Exception("No novelty functions specified");
            }
        }
    }

    @Override
    public void step() {
        super.step();
        fitnessFunction.step();
        for (InformedCharacterisation nf : noveltyFunctions) {
            nf.step();
        }
    }

    @Override
    public void setSimulatorExperiment(Simulation exp) {
        super.setSimulatorExperiment(exp);
        fitnessFunction.setSimulatorExperiment(exp);
        for (InformedCharacterisation nf : noveltyFunctions) {
            nf.setSimulatorExperiment(exp);
        }
    }

    @Override
    public void evaluate() {
        // Do nothing - the other functions do the evaluations
    }

    @Override
    public double[] getBehaviour() {
        if (splits == null) {
            splits = new int[noveltyFunctions.length];
            splits[0] = 0;
            int i = 1;
            for (InformedCharacterisation f : noveltyFunctions) {
                if (i < splits.length) {
                    splits[i] = splits[i - 1] + f.getBehaviour().length;
                }
            }
        }
        splits = new int[noveltyFunctions.length];
        splits[0] = 0;
        double[] b = noveltyFunctions[0].getBehaviour();
        for (int i = 1; i < noveltyFunctions.length; i++) {
            splits[i] = b.length;
            b = ArrayUtils.addAll(b, noveltyFunctions[i].getBehaviour());
        }
        return b;
    }

    @Override
    public double getFitness() {
        return fitnessFunction.getFitness();
    }

    @Override
    public int[] splitIndexes() {
        return splits;
    }
}
