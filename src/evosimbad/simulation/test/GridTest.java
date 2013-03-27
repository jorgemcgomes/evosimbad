/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.simulation;

import java.util.ArrayList;
import java.util.List;
import javax.media.j3d.Transform3D;
import javax.media.j3d.VirtualUniverse;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFTask;

/**
 *
 * @author Jorge
 */
public class GridTest {

    public static final int TASKS = 10000;

    public static void main(String... args) throws JPPFException, Exception {
        JPPFClient client = new JPPFClient();
        int i = 0;
        System.out.println("NEW JOB " + i);
        JPPFJob job = new JPPFJob();
        job.setName("test" + i++);
        for (int j = 0; j < TASKS; j++) {
            job.addTask(new TestTask());
        }
        job.setBlocking(true);
        System.out.println("Submiting " + job.getTasks().size() + " tasks");
        List<JPPFTask> submit = client.submit(job);
        System.out.println("Done... reading");
        for (JPPFTask t : submit) {
            if (t.getException() != null) {
                t.getException().printStackTrace();
            } else {
                System.out.println(((TestTask) t).status);
            }
        }
        client.close();
    }

    public static class TestTask extends JPPFTask {
        
        public boolean status = false;

        @Override
        public void run() {
            //System.out.println("received");
            this.status = true;
            /*Transform3D t = new Transform3D();
            t.set(0);
            try {
                Class.forName("javax.media.j3d.VirtualUniverse");
            } catch (ClassNotFoundException ex) {
                status = false;
                ex.printStackTrace();
            }
            VirtualUniverse un = new VirtualUniverse();
            List<Object> test = new ArrayList<>();
            test.add(1);*/
        }
    }
}
