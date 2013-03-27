/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.aggregation.test;

import evosimbad.core.EvaluationFunction;
import java.util.ArrayList;
import javax.vecmath.Point3d;
import simbad.sim.Agent;

/**
 *
 * @author Jorge
 */
public class CenterMassEvaluation extends EvaluationFunction {

    private double fitness = 0;
    
    public CenterMassEvaluation(int updateRate) {
        super(updateRate);
    }
    
    @Override
    public void evaluate() {
        fitness += averageDistanceToCenterMass(experiment.getEnvironment().getAgents());
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

    @Override
    public double getFitness() {
        return -fitness / getTotalEvaluations();
    }
    
}
