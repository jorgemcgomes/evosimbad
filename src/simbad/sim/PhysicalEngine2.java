/*
 *  Simbad - Robot Simulator
 *  Copyright (C) 2004 Louis Hugues
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 -----------------------------------------------------------------------------
 * $Author: sioulseuguh $ 
 * $Date: 2005/08/07 12:24:57 $
 * $Revision: 1.3 $
 * $Source: /cvsroot/simbad/src/simbad/sim/PhysicalEngine.java,v $
 */
package simbad.sim;

import java.util.ArrayList;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * Centralize resources and algorithms related to physical interactions. Most of
 * this is experimental for the time being.
 */
public class PhysicalEngine2 {

    /**
     * gravitational acceleration constant in m/s**2
     */
    protected final static float g = 9.8f;
    public static double AGENT_FRICTION = 0.5; // 0 - total , 1 - no friction    
    public static double OBJECT_FRICTION = 0.2;
    double epsilonContact = 0.00001;
    // temp 3d resources
    private BoundingSphere bs1, bs2;
    private Vector3d v1;
    private Vector3d v2;
    private Vector3d v3;
    private Vector3d v4;
    private Vector3d v5;
    private Point3d p1;
    private Point3d p2;
    private Point3d p3;
    private Point3d p4;
    private Point3d p5;

    PhysicalEngine2() {
        // Allocate some permanent resources to avoid gc.
        p1 = new Point3d();
        p2 = new Point3d();
        p3 = new Point3d();
        p4 = new Point3d();
        p5 = new Point3d();
        v1 = new Vector3d();
        v2 = new Vector3d();
        v3 = new Vector3d();
        v4 = new Vector3d();
        v5 = new Vector3d();
        bs1 = new BoundingSphere();
        bs2 = new BoundingSphere();
    }

    /**
     * Compute all external force contributions on an agent before any impact.
     *
     * @param dt virtual time elapsed since last call.
     */
    protected void computeForces(double dt, SimpleAgent a) {
        // Gravity
        // apply F = mg  pointing down .
        if (a.distanceToGround() > 0) {

            v1.set(0, -a.mass * g, 0);
            a.linearAcceleration.add(v1);
        }

        // Friction 
        // apply friction reaction if velocity>0
        if ((a.staticFrictionCoefficient > 0)
                && (a.linearVelocity.lengthSquared() > 0)) {
            // Friction reaction is fx|N| - with N = mg
            float reaction = a.mass * g * a.staticFrictionCoefficient;
            // It is colinear to Velocity vector and in opposite direction.
            // Obtain a unit vector oriented like velocity.
            v1.set(a.linearVelocity);
            v1.normalize();
            // scale it to reaction
            v1.scale(reaction);
            a.linearAcceleration.sub(v1);
        }

    }

    protected void clearCollisions(ArrayList agents) {
        for (Object oAg : agents) {
            Agent ag = (Agent) oAg;
            ag.collisionDetected = false;
            ag.clearVeryNear();
        }
    }

    protected boolean agentObjectCollisions(SimpleAgent ag, ArrayList objects, boolean computeImpact) {
        int nobjs = objects.size();
        boolean any = false;

        // posicao prevista
        bs1.set(ag.getBounds());
        bs1.transform(ag.translation);
        bs1.getCenter(p1);
        p1.add(ag.instantTranslation);
        bs1.setCenter(p1);

        // check each object
        for (int j = 0; j < nobjs; j++) {
            Object o = objects.get(j);
            if (o instanceof StaticObject) {
                if (!((StaticObject) o).getCanBeTraversed() && ((StaticObject) o).intersect(bs1)) {
                    ag.collisionDetected = true;
                    any = true;
                    if (computeImpact) {
                        computeAgentObjectImpact(ag, (StaticObject) o, bs1);
                    }
                }
            }
        }
        return any;
    }

    private void computeAgentObjectImpact(SimpleAgent a, StaticObject o,
            BoundingSphere bsa) {
        Vector3d n = v5;

        double friction = OBJECT_FRICTION * (0.5 + Math.random() / 0.5);

        //  Surface normal
        computeContactNormal(bsa, o.getTransformedBounds(), n);

        // Impact direction
        Matrix3d m = new Matrix3d();
        m.rotY(Math.PI / 2);
        Transform3D tr = new Transform3D();
        tr.setRotation(m);
        Vector3d impactDirection = new Vector3d(n);
        tr.transform(impactDirection);
        impactDirection.normalize();
        impactDirection.absolute();

        // velocity component in impact direction 
        double ndotva = impactDirection.dot(a.linearVelocity);
        v2.set(impactDirection);
        v2.scale(ndotva * friction);
        a.linearVelocity.set(v2);
    }
    private Point3d[] corners = {new Point3d(), new Point3d(), new Point3d(), new Point3d()};

    protected void computeContactNormal(BoundingSphere bs, Bounds object, Vector3d n) {
        if (object instanceof BoundingBox) {
            BoundingBox bb = (BoundingBox) object;
            bb.getLower(p1);
            bb.getUpper(p2);
            bs.getCenter(p3);
            //System.out.println(p1 + " " + p2);

            // box corners
            corners[0].set(p1); // left bottom
            corners[2].set(p2); // right top
            corners[1].set(p1.x, p3.y, p2.z); // left top
            corners[3].set(p2.x, p3.y, p1.z); // right bottom

            int hits = 0;
            for (int i = 0; i < 4; i++) {
                Point3d a = corners[i];
                Point3d b = corners[(i + 1) % 4];
                if (intersects(a, b, bs)) {
                    n.set(leftNormal(a, b));
                    hits++;
                }
            }

            if (hits > 1) {
                Matrix3d m = new Matrix3d();
                m.rotY(Math.PI / 4);
                Transform3D tr = new Transform3D();
                tr.setRotation(m);
                tr.transform(n);
            }

            n.normalize();
        }
    }

    // WARNING: Math.round apenas funciona quando todos os obstaculos sao axis aligned
    private Vector3d leftNormal(Point3d p1, Point3d p2) {
        v1.sub(p2, p1); // de p1 para p2
        v1.set(Math.round(-v1.z), Math.round(v1.y), Math.round(v1.x));
        return v1;
    }
    private BoundingBox bb = new BoundingBox();

    private boolean intersects(Point3d a, Point3d b, BoundingSphere ag) {
        double c = 0.001;
        p4.set(Math.min(a.x, b.x) - c, -10, Math.min(a.z, b.z) - c);
        p5.set(Math.max(a.x, b.x) + c, 10, Math.max(a.z, b.z) + c);
        bb.setLower(p4);
        bb.setUpper(p5);
        return bb.intersect(ag);
    }

    protected boolean agentAgentsCollisions(SimpleAgent ag, ArrayList agents, boolean computeImpact) {
        ag.interactionDetected = false;

        // At least two agents
        if (agents.size() >= 2 && !ag.detachedFromSceneGraph) {

            // predicted agent position
            bs1.set(ag.getBounds());
            bs1.transform(ag.translation);
            bs1.getCenter(p1);
            p1.add(ag.instantTranslation);
            bs1.setCenter(p1);

            for (int i = 0; i < agents.size(); i++) {
                SimpleAgent other = ((SimpleAgent) agents.get(i));
                if (other == ag || other.detachedFromSceneGraph) {
                    continue;
                }
                // at least one of them has moved.
                if ((ag.positionChanged) || (other.positionChanged)) {
                    // posicao actual do other
                    bs2.set(other.getBounds());
                    bs2.transform(other.translation);

                    // Se vao intersectar                
                    if (bs1.intersect(bs2)) {
                        ag.veryNear(other);
                        other.veryNear(ag);

                        if (!(ag.canBeTraversed || other.canBeTraversed)) {
                            ag.collisionDetected = true;
                            other.collisionDetected = true;
                            ag.interactionDetected = true;
                            other.interactionDetected = true;

                            if (computeImpact) {
                                computeAgentAgentImpact(ag, other, bs1, bs2);
                            }
                        }
                    }
                }
            }
        }
        return ag.interactionDetected;
    }

    private void computeAgentAgentImpact(SimpleAgent a1, SimpleAgent a2,
            BoundingSphere bs1, BoundingSphere bs2) {

        computeImpact(a1, a2, bs1, bs2);
        computeImpact(a2, a1, bs2, bs1);
    }

    private void computeImpact(SimpleAgent ag, SimpleAgent other,
            BoundingSphere bsAg, BoundingSphere bsOther) {

        double friction = AGENT_FRICTION * (0.5 + Math.random() / 0.5);

        bsAg.getCenter(p1);
        bsOther.getCenter(p2);
        Vector3d n = v1; // from other to ag
        n.sub(p1, p2);
        n.normalize();
        Vector3d normal = v2;
        v2.set(n.z, n.y, -n.x); // para a direita

        Vector3d vel = v3;
        vel.set(ag.linearVelocity);
        Vector3d perpToVel = v4;
        perpToVel.set(vel.z, vel.y, -vel.x);
        vel.add(p1);
        perpToVel.add(p1);

        if (isLeft(p1, perpToVel, p2)) { // o other esta a esquerda do ag
            // desloca-se perpendicularmente a normal entre os agentes
            n.scale(ag.linearVelocity.length() * friction);
            ag.linearVelocity.set(n);
        } else { // o sentido da velocity afasta-se do other
            // desloca-se paralelamente a normal entre os dois agentes
            if (isLeft(p1, vel, p2)) { // esta com uma velocity na direccao do other
                // vai para a direita
                normal.scale(ag.linearVelocity.length() * friction);
            } else { // o other esta a direita do ag
                // vai para a esquerda
                normal.scale(-ag.linearVelocity.length() * friction);
            }
            ag.linearVelocity.set(normal);

        }
    }

    private boolean isLeft(Tuple3d a, Tuple3d b, Tuple3d p) {
        return ((b.x - a.x) * (p.z - a.z) - (b.z - a.z) * (p.x - a.x)) < 0;
    }
}