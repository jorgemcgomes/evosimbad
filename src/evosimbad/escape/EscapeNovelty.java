/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.escape;

import evosimbad.escape.EscapeAgentGenerator.EscapeAgent;
import evosimbad.escape.EscapeEnvironment.EscapeEnv;
import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.vecmath.Point3d;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class EscapeNovelty extends InformedCharacterisation {

    // quantos robos passam
    // instante em que o primeiro passa
    // 
    private int totalAgents = 0;
    private int escapedAgents = 0;
    private int closeTime = 0;
    private int firstTime = -1;
    private int lastTime = -1;
    private double distanceToDoor;
    private double escapeWindow = -1;
    private transient Set<Agent> escaped = new HashSet<>();

    public EscapeNovelty() {
        super(1);
    }

    @Override
    public double[] getBehaviour() {
        double fugitives = (double) escapedAgents / totalAgents;
        double close = (double) closeTime / getTotalEvaluations();
        double dist = distanceToDoor / closeTime;
        double speed = (lastTime - firstTime) / (escapeWindow * 10);
        return new double[]{fugitives, close, dist, speed};
    }

    @Override
    public void evaluate() {
        EscapeEnv env = (EscapeEnv) experiment.getEnvironment();
        ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
        escapeWindow = env.getEscapeWindow();

        // door open
        for (Agent ag : agents) {
            EscapeAgent ea = (EscapeAgent) ag;
            if (!escaped.contains(ea) && ea.getStatus() == EscapeAgent.ESCAPED) { // ea just escaped
                escaped.add(ea);
                lastTime = getCurrentEvaluation();
                if (firstTime == -1) { // it was the first to escape
                    firstTime = getCurrentEvaluation();
                }
            }
        }

        // door closed
        if (closeTime == 0 && env.getDoor().isClosed()) {
            closeTime = getCurrentEvaluation();
        }

        // count the fugitives
        if (getCurrentEvaluation() == getTotalEvaluations()) {
            totalAgents = experiment.getEnvironment().getAgents().size();
            for (Agent ag : experiment.getEnvironment().getAgents()) {
                EscapeAgent ea = (EscapeAgent) ag;
                if (ea.getStatus() == EscapeAgentGenerator.EscapeAgent.ESCAPED) {
                    escapedAgents++;
                }
            }
            if (closeTime == 0) {
                closeTime = getTotalEvaluations();
            }
        }

        // door distance
        Point3d p = new Point3d();
        double averageDistance = 0;
        int running = 0;
        for (Agent ag : agents) {
            EscapeAgent ea = (EscapeAgent) ag;
            if (ea.getStatus() == EscapeAgent.RUNNING) {
                running++;
                ag.getCoords(p);
                double d = p.distance(env.getDoorLocation()) / env.worldSize;
                averageDistance += d;
            }

        }
        if (running > 0) {
            averageDistance /= running;
            distanceToDoor += averageDistance;
        }
    }

    @Override
    public double getFitness() {
        return 0;
    }
}
