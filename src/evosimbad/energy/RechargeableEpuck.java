/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.energy;

import evosimbad.commons.Epuck;
import javax.vecmath.Vector3d;
import org.apache.commons.lang3.ArrayUtils;
import simbad.sim.DummySensor;
import simbad.sim.DummySensor.SensorReader;
import simbad.sim.IRBeltSensorBounds;

/**
 *
 * @author Jorge
 */
public class RechargeableEpuck extends Epuck {

    protected double energyLevel, initialLevel, maxLevel, dropRate;
    protected IRBeltSensorBounds stationSensor;
    protected DummySensor<Double> energySensor;
    protected DummySensor<Integer> chargingSensor;
    protected boolean isCharging;

    public RechargeableEpuck(double initialLevel, double maxLevel, double dropRate) {
        super(5);
        this.energyLevel = initialLevel;
        this.maxLevel = maxLevel;
        this.dropRate = dropRate;
        this.initialLevel = initialLevel;
    }

    public void charge(double energy) {
        energyLevel = Math.min(maxLevel, energyLevel + energy);
    }

    @Override
    public void reset() {
        super.reset();
        energyLevel = initialLevel;
    }

    public double getEnergy() {
        return energyLevel;
    }

    public boolean isCharging() {
        return isCharging;
    }

    @Override
    protected void performBehavior(double[] output) {
        if (energyLevel > 0.1) {
            behavior(output);
            energyLevel = Math.max(0, energyLevel - dropRate);
        } else {
            kinematics.setLeftVelocity(0);
            kinematics.setRightVelocity(0);
        }
    }

    protected void behavior(double[] output) {
        super.performBehavior(output);
    }

    public void setChargingStationSensor(float range, int nSensors) {
        Vector3d pos = new Vector3d(0, 0, 0);
        stationSensor = new IRBeltSensorBounds(0.01f, range, 0.01f, nSensors, PointOfCharge.class, false);
        stationSensor.setUpdatePerSecond(updatesPerSecond);
        super.addSensorDevice(stationSensor, pos, 0);
    }

    public double[] readChargingStationSensor() {
        double[] ir = null;
        if (stationSensor != null) {
            ir = stationSensor.getQuadrantsMeasurements();
            for (int i = 0; i < ir.length; i++) {
                ir[i] = Double.isInfinite(ir[i]) ? 1 : normalize(ir[i], 0, stationSensor.getMaxRange());
            }
        }
        return ir;
    }

    public void setEnergySensor() {
        Vector3d pos = new Vector3d(0, 0, 0);
        energySensor = new DummySensor<>("Energy", 0d, new SensorReader<Double>() {
            @Override
            public Double getReading() {
                return energyLevel;
            }
        });
        energySensor.setUpdatePerSecond(updatesPerSecond);
        super.addSensorDevice(energySensor, pos, 0);
    }

    public double readEnergySensor() {
        return normalize(energySensor.read(), 0, maxLevel);
    }

    public void setChargingSensor() {
        Vector3d pos = new Vector3d(0, 0, 0);
        chargingSensor = new DummySensor<>("Charging", 0, new SensorReader<Integer>() {
            @Override
            public Integer getReading() {
                return isCharging ? 1 : 0;
            }
        });
        chargingSensor.setUpdatePerSecond(updatesPerSecond);
        super.addSensorDevice(chargingSensor, pos, 0);
    }

    public double readChargingSensor() {
        return normalize(chargingSensor.read(), 0, 1);
    }

    @Override
    public double[] readSensors() {
        double[] sens = super.readSensors();
        if (stationSensor != null) {
            sens = ArrayUtils.addAll(sens, readChargingStationSensor());
        }
        if (energySensor != null) {
            sens = ArrayUtils.add(sens, readEnergySensor());
        }
        if (chargingSensor != null) {
            sens = ArrayUtils.add(sens, readChargingSensor());
        }
        return sens;
    }
}
