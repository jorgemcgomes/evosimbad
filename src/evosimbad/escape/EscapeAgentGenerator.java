/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.escape;

import evosimbad.commons.Epuck;
import evosimbad.commons.VariableAgentGenerator;
import evosimbad.core.NNAgent;
import javax.media.j3d.BranchGroup;
import javax.vecmath.Vector3d;
import org.apache.commons.lang3.ArrayUtils;
import simbad.sim.IRBeltSensorBounds;

/**
 *
 * @author jorge
 */
public class EscapeAgentGenerator extends VariableAgentGenerator {

    private float doorSensorRange;

    public EscapeAgentGenerator(int min, int max, float doorSensorRange) {
        super(min, max);
        this.doorSensorRange = doorSensorRange;
    }

    @Override
    protected NNAgent generateAgent() {
        EscapeAgent ag = new EscapeAgent();
        ag.setObstacleIR(0.10f, 8);
        ag.setRobotIR(0.25f, 24);
        ag.setDoorSensor(doorSensorRange, 16);
        ag.setNearCount(0.25f + ag.getRadius() * 2, -1);
        ag.toggleStopActuator(true);
        return ag;
    }

    @Override
    public int getInputs() {
        return 8 + 8 + 8 + 1;
    }

    @Override
    public int getOutputs() {
        return 3;
    }

    public static class EscapeAgent extends Epuck {

        public static int RUNNING = 0, ESCAPED = 1, INSIDE = 2;
        private IRBeltSensorBounds doorSensor;
        private int status = RUNNING;

        public EscapeAgent() {
            super(5);
        }

        public void setDoorSensor(float range, int nSensors) {
            Vector3d pos = new Vector3d(0, 0, 0);
            doorSensor = new IRBeltSensorBounds(getRadius(), range, 0.01f, nSensors, EscapeEnvironment.Door.class, false);
            doorSensor.setUpdatePerSecond(updatesPerSecond);
            super.addSensorDevice(doorSensor, pos, 0);
        }

        public double[] readDoorSensor() {
            double[] ir = null;
            if (doorSensor != null) {
                ir = doorSensor.getQuadrantsMeasurements();
                for (int i = 0; i < ir.length; i++) {
                    ir[i] = Double.isInfinite(ir[i]) ? 1 : normalize(ir[i], 0, doorSensor.getMaxRange());
                }
            }
            return ir;
        }

        @Override
        public double[] readSensors() {
            double[] sens = super.readSensors();
            if (doorSensor != null) {
                sens = ArrayUtils.addAll(sens, readDoorSensor());
            }
            return sens;
        }

        @Override
        protected void performBehavior() {
            if (status == RUNNING) {
                super.performBehavior();
            } else {
                super.kinematics.setLeftVelocity(0);
                super.kinematics.setRightVelocity(0);
            }
        }

        @Override
        protected void updateSensors(double elapsedSecond, BranchGroup pickableSceneBranch) {
            if (status == RUNNING) {
                super.updateSensors(elapsedSecond, pickableSceneBranch);
            }
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
