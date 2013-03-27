/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.dispersion;

import evosimbad.core.EvaluationFunction;
import java.util.ArrayList;
import javax.vecmath.Point3d;
import simbad.sim.Agent;

/**
 *
 * @author jorge
 */
public class DispersionFitness extends EvaluationFunction {

    private double targetDispersion;
    private double fitness;
    
    public DispersionFitness(double targetDispersion) {
        super();
        this.targetDispersion = targetDispersion;
    }

    @Override
    public void evaluate() {
        if(super.getCurrentEvaluation() == super.getTotalEvaluations()) { // only measure in last evaluation
            ArrayList<Agent> agents = experiment.getEnvironment().getAgents();
            double maxDistance = experiment.getEnvironment().worldSize - agents.iterator().next().getRadius() * 4;
            double averageScore = 0;
            for(Agent a1 : agents) {
                double closest = Double.POSITIVE_INFINITY;
                for(Agent a2 : agents) {
                    if(a1 != a2) {
                        Point3d p1 = new Point3d();
                        a1.getCoords(p1);
                        Point3d p2 = new Point3d();
                        a2.getCoords(p2);
                        // descontar os raios dos robos e a dist minima e zero
                        double d = Math.max(0, p1.distance(p2) - a1.getRadius() - a2.getRadius());
                        closest = Math.min(closest, d);
                    }
                }              
                // na distancia ideal -- fitness = 1
                // na maxima separacao -- fitness = 0
                // colados um ao outro -- fitness = 0
                if(closest < targetDispersion) {
                    averageScore += closest / targetDispersion;
                } else {
                    averageScore += 1 - (closest - targetDispersion) / (maxDistance - targetDispersion);
                }
            }
            fitness = averageScore / agents.size();
        }
    }

    @Override
    public double getFitness() {
        return fitness;
    }
    
}
