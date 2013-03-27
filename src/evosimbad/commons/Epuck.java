/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import javax.vecmath.Vector3d;
import org.apache.commons.lang3.ArrayUtils;
import simbad.sim.IRBeltSensorBounds;
import simbad.sim.NearCountSensor;
import simbad.sim.StaticObject;
import simbad.sim.Wall;

/**
 *
 * @author Jorge
 */
public class Epuck extends EpuckBase {

    protected IRBeltSensorBounds obstacleIR;
    protected IRBeltSensorBounds robotIR;
    protected NearCountSensor nearCount;

    public Epuck(int updatesPerSecond) {
        super(updatesPerSecond);
    }

    public void setObstacleIR(float range, int nSensors) {
        Vector3d pos = new Vector3d(0, 0, 0);
        obstacleIR = new IRBeltSensorBounds(getRadius(), range, 0.01f, nSensors, Wall.class, false);
        obstacleIR.setUpdatePerSecond(updatesPerSecond);
        super.addSensorDevice(obstacleIR, pos, 0);
    }

    public void setRobotIR(float range, int nSensors) {
        Vector3d pos = new Vector3d(0, 0, 0);
        robotIR = new IRBeltSensorBounds(getRadius(), range, 0.01f, nSensors, Epuck.class, false);
        robotIR.setUpdatePerSecond(updatesPerSecond);
        super.addSensorDevice(robotIR, pos, 0);
    }

    public void setNearCount(float range, int countNormalization) {
        Vector3d pos = new Vector3d(0, 0, 0);
        nearCount = new NearCountSensor(range, Epuck.class, countNormalization);
        nearCount.setUpdatePerSecond(updatesPerSecond);
        super.addSensorDevice(nearCount, pos, 0);
    }

    public double[] readObstacleIR() {
        double[] ir = null;
        if (obstacleIR != null) {
            ir = obstacleIR.getQuadrantsMeasurements();
            for (int i = 0; i < ir.length ; i++) {
                ir[i] = Double.isInfinite(ir[i]) ? 1 : normalize(ir[i], 0, obstacleIR.getMaxRange()); // o 1 estava a 2
            }
        }
        return ir;
    }

    public double[] readRobotIR() {
        double[] ir = null;
        if (robotIR != null) {
            ir = robotIR.getQuadrantsMeasurements();
            for (int i = 0; i < ir.length ; i++) {
                ir[i] = Double.isInfinite(ir[i]) ? 1 : normalize(ir[i], 0, robotIR.getMaxRange()); // o 1 estava a 2
            }
        }
        return ir;
    }

    public double readNearCount() {
        return normalize(nearCount.getNearCountNormalized(), 0, 1);
    }

    @Override
    public double[] readSensors() {
        double[] sens = ArrayUtils.EMPTY_DOUBLE_ARRAY;
        if(obstacleIR != null) {
            sens = ArrayUtils.addAll(sens, readObstacleIR());
        }
        if(robotIR != null) {
            sens = ArrayUtils.addAll(sens, readRobotIR());
        }
        if(nearCount != null) {
            sens = ArrayUtils.add(sens, readNearCount());
        }
        return sens;
    }

    public NearCountSensor getNearCount() {
        return nearCount;
    }

    public IRBeltSensorBounds getObstacleIR() {
        return obstacleIR;
    }

    public IRBeltSensorBounds getRobotIR() {
        return robotIR;
    }
}
