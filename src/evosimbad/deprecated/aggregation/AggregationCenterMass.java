/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.deprecated.aggregation;

import evosimbad.evolution.InformedCharacterisation;
import java.util.ArrayList;
import javax.vecmath.Point3d;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class AggregationCenterMass extends InformedCharacterisation {

    private double fitnessWCount = 0;
    private double fitness = 0;
    private double[] behaviour;
    private int index = 0;  
    private double normalizationConstant = -1;
    
    public AggregationCenterMass(int updateRate) {
        super(updateRate);
    }
    
    @Override
    public void evaluate() {
        if(normalizationConstant == -1) { // normalizes between 0 and 1
            double size = experiment.getEnvironment().worldSize;
            normalizationConstant = 1 / (Math.sqrt(size*size*2) / 2);
        }
        double w = experiment.getCurrentStep() / (double) experiment.getTotalSteps();
        double dist = averageDistanceToCenterMass(experiment.getEnvironment().getAgents()) * normalizationConstant ;
        fitness += dist * w;
        fitnessWCount += w;
        if (behaviour == null) {
            behaviour = new double[getTotalEvaluations()];
        }
        behaviour[index++] = dist;
    }    
    
    @Override
    public double[] getBehaviour() {
        return behaviour;
    }

    @Override
    public double getFitness() {
        return 1 - (fitness / fitnessWCount);
    }

    static double averageDistanceToCenterMass(ArrayList<Agent> agents) {
        // Determinar centro de massa
        Point3d centerOfMass = new Point3d();
        Point3d pt = new Point3d();
        for(Agent a : agents) {
            a.getCoords(pt);
            centerOfMass.x += pt.x;
            centerOfMass.y += pt.y; 
            centerOfMass.z += pt.z;
        }
        centerOfMass.x /= agents.size();
        centerOfMass.y /= agents.size();
        centerOfMass.z /= agents.size();
        
        // Determinar a distancia medio ao centro de massa
        double averageDistance = 0;
        for(Agent a : agents) {
            a.getCoords(pt);
            averageDistance += centerOfMass.distance(pt);
        }
        averageDistance /= agents.size();
        return averageDistance;
    }    
}
