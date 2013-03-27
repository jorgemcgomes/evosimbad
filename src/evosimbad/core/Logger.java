/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

import java.io.*;
import org.apache.commons.lang3.tuple.Pair;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 *
 * @author Jorge
 */
public abstract class Logger {

    protected ComponentLoader main;

    protected void setMain(ComponentLoader exp) {
        this.main = exp;
    }

    public abstract void appendLine(String log, Object... cols);

    public abstract void flush();

    public abstract void saveLogs() throws IOException;
    
    public abstract void saveIndividual(NeuralNet individual, String name);
    
    public abstract void saveObject(Serializable obj, String name);
    
    public abstract void traceBehaviour(NeuralNet individual, String name);

    protected void saveIndividual(NeuralNet individual, File file) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        String conf = "# " + main.getCreationDate() + "\n"
                + main.getConfig();
        oos.writeObject(conf);
        oos.writeObject(individual);
        oos.close();
    }

    public static Pair<NeuralNet, String> loadIndividual(File file) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        String conf = (String) ois.readObject();
        NeuralNet ind = (NeuralNet) ois.readObject();
        ois.close();
        Pair<NeuralNet, String> loaded = Pair.of(ind, conf);
        return loaded;
    }
}
