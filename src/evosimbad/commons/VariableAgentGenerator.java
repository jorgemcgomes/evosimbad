/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import evosimbad.core.AgentGenerator;
import evosimbad.core.NNAgent;
import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.vecmath.Color3f;

/**
 *
 * @author Jorge
 */
public abstract class VariableAgentGenerator implements AgentGenerator {

    public static Color[] colors = new Color[]{Color.BLUE, Color.RED, Color.PINK, Color.CYAN, Color.BLACK, Color.ORANGE, Color.MAGENTA, Color.GRAY, Color.YELLOW, Color.DARK_GRAY, Color.LIGHT_GRAY};
    public static String[] colorNames = new String[]{"blue", "red", "pink", "cyan", "black", "orange", "magenta", "gray", "yellow", "darkGray", "lightGray"};
    private int minRobots, maxRobots;
    private LinkedList<Integer> numberQueue = new LinkedList<>();

    public VariableAgentGenerator(int min, int max) {
        this.minRobots = min;
        this.maxRobots = max;
    }

    @Override
    public synchronized Set<NNAgent> generateAgents() {
        if (numberQueue.isEmpty()) {
            for (int i = minRobots; i <= maxRobots; i++) {
                numberQueue.add(i);
            }
            Collections.shuffle(numberQueue);
        }
        HashSet<NNAgent> agents = new HashSet<>();
        int number = numberQueue.pollFirst();
        for (int i = 0; i < number; i++) {
            Color color = colors[i % colors.length];
            String name = "";
            if (i < colors.length) {
                name = "ag_" + colorNames[i % colors.length];
            } else {
                name = "ag_" + colorNames[i % colors.length] + "_" + (i / colors.length);
            }
            NNAgent agent = generateAgent();
            agent.setName(name);
            agent.setColor(new Color3f(color));
            agents.add(agent);
        }
        return agents;
    }
    
    protected abstract NNAgent generateAgent();
}
