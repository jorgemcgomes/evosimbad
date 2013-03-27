/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.collaboration;

import evosimbad.commons.Epuck;
import evosimbad.energy.PointOfCharge;
import java.util.ArrayList;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.commons.lang3.ArrayUtils;
import simbad.sim.DummySensor;
import simbad.sim.DummySensor.SensorReader;
import simbad.sim.IRBeltSensorBounds;

/**
 *
 * @author jorge
 */
public class CollaborativeEpuck extends Epuck {

    protected IRBeltSensorBounds taskSensor;
    protected DummySensor<Integer> overTask;
    protected boolean isOverTask = false;

    public CollaborativeEpuck() {
        super(5);
        super.toggleStopActuator(true);
    }

    public boolean isOverTask() {
        return isOverTask;
    }

    public void setOverTaskSensor() {
        Vector3d pos = new Vector3d(0, 0, 0);
        overTask = new DummySensor<>("Over task: ", 0, new SensorReader<Integer>() {
            @Override
            public Integer getReading() {
                Point3d self = new Point3d();
                getCoords(self);
                ArrayList agentList = getSimulator().getAgentList();
                isOverTask = false;
                for (Object o : agentList) {
                    if (o instanceof TaskSpot) {
                        TaskSpot ts = (TaskSpot) o;
                        Point3d p = new Point3d();
                        ts.getCoords(p);
                        double dist = self.distance(p);
                        if (dist <= ts.getRadius() - getRadius()) {
                            isOverTask = true;
                        }
                    }
                }
                return isOverTask ? 1 : 0;
            }
        });
        overTask.setUpdatePerSecond(updatesPerSecond);
        super.addSensorDevice(overTask, pos, 0);
    }

    public double readOverTaskSensor() {
        return normalize(overTask.read(), 0, 1);
    }

    public void setTaskSensor(float range, int nSensors) {
        Vector3d pos = new Vector3d(0, 0, 0);
        taskSensor = new IRBeltSensorBounds(getRadius(), range, 0.01f, nSensors, TaskSpot.class, false);
        taskSensor.setUpdatePerSecond(updatesPerSecond);
        super.addSensorDevice(taskSensor, pos, 0);
    }

    public double[] readTaskSensor() {
        double[] ir = null;
        if (taskSensor != null) {
            ir = taskSensor.getQuadrantsMeasurements();
            for (int i = 0; i < ir.length; i++) {
                ir[i] = Double.isInfinite(ir[i]) ? 1 : normalize(ir[i], 0, taskSensor.getMaxRange());
            }
        }
        return ir;
    }

    @Override
    public double[] readSensors() {
        double[] sens = super.readSensors();
        if (taskSensor != null) {
            sens = ArrayUtils.addAll(sens, readTaskSensor());
        }
        if (overTask != null) {
            sens = ArrayUtils.add(sens, readOverTaskSensor());
        }
        return sens;
    }
}
