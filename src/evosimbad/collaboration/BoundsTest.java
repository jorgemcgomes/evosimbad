/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.collaboration;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

/**
 *
 * @author jorge
 */
public class BoundsTest {
    
    public static void main(String[] args) {
        Bounds b = new BoundingSphere(new Point3d(0,0,0), 1d);
        Point3d test = new Point3d(0,0,0.3);
        System.out.println(b.intersect(test));
    }
    
}
