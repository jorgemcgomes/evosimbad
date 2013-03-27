/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.commons;

import au.com.bytecode.opencsv.CSVWriter;
import evosimbad.core.ComponentLoader;
import evosimbad.core.Logger;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.neat4j.neat.nn.core.NeuralNet;

/**
 *
 * @author Jorge
 */
public class CSVLogger extends Logger {

    private String tempDir, expDir;
    private File tempFolder;
    private File individualsFolder;
    private File tracesFolder;
    private File baseFolder;
    private HashMap<String, Integer> indNames = new HashMap<>();
    private ImageTracer tracer;
    private HashMap<String, CSVWriter> writers = new HashMap<>();

    public CSVLogger(String tempDir, String expDir) {
        this.tempDir = tempDir;
        this.expDir = expDir;
    }

    public File getBaseFolder() {
        return baseFolder;
    }
    
    public File getTempFolder() {
        return tempFolder;
    }

    @Override
    protected void setMain(ComponentLoader exp) {
        super.setMain(exp);
        baseFolder = new File(expDir);
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        tempFolder = new File(tempDir, df.format(exp.getCreationDate()));
        if (!tempFolder.exists()) {
            tempFolder.mkdirs();
        }
        individualsFolder = new File(tempFolder, "Individuals");
        if (!individualsFolder.exists()) {
            individualsFolder.mkdirs();
        }
        tracesFolder = new File(tempFolder, "Traces");
        if (!tracesFolder.exists()) {
            tracesFolder.mkdirs();
        }

        File confFile = new File(tempFolder, "configuration.conf");
        String conf = "# " + exp.getCreationDate().toString() + "\n" + exp.getConfig();
        try {
            FileUtils.write(confFile, conf);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        tracer = new ImageTracer(main.getSimulationBuilder());
    }

    @Override
    public void appendLine(String log, Object... cols) {
        CSVWriter writer = writers.get(log);
        if (writer == null) {
            try {
                File f = new File(tempFolder, log + ".csv");
                f.getParentFile().mkdirs();
                writer = new CSVWriter(new BufferedWriter(new FileWriter(f)), '\t');
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            writers.put(log, writer);
        }

        String[] strs = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            if(cols[i] == null) {
                strs[i] = "NULL";
            } else {
                strs[i] = cols[i].toString();
            }
        }
        writer.writeNext(strs);
    }

    @Override
    public void flush() {
        for (CSVWriter writer : writers.values()) {
            try {
                writer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void saveLogs() throws IOException {
        FileUtils.copyDirectoryToDirectory(tempFolder, baseFolder);
    }

    @Override
    public void saveIndividual(NeuralNet individual, String name) {
        String fullName;
        if (!indNames.containsKey(name)) {
            fullName = name + ".ind";
            indNames.put(name, 0);
        } else {
            int count = indNames.get(name);
            fullName = name + "_" + count + ".ind";
            indNames.put(name, count + 1);
        }
        File f = new File(individualsFolder, fullName);
        f.getParentFile().mkdirs();
        try {
            super.saveIndividual(individual, f);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    

    @Override
    public void traceBehaviour(NeuralNet individual, String name) {
        tracer.trace(individual, name, tracesFolder);
    }

    @Override
    public void saveObject(Serializable obj, String name) {
        ObjectOutputStream oos = null;
        try {
            File f = new File(tempFolder, name);
            f.getParentFile().mkdirs();
            oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(obj);
            oos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
