package org.neat4j.neat.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.neat4j.core.AIConfig;
import org.neat4j.core.AIConfigurationLoader;

/**
 * @author MSimmerson
 *
 * Loads the configuration specified file and creates a configuration object
 */
public class NEATLoader implements AIConfigurationLoader {

	/**
	 * @see org.neat4j.ailibrary.core.AIConfigurationLoader#loadConfig(java.lang.String)
	 */
	public AIConfig loadConfig(String location) {
		AIConfig config = null;
		try {
			config = new NEATConfig(this.createConfig(location));
		} catch (IOException e) {
			// create default network
			System.out.println("Creating Default Config");
			config = new NEATConfig();
		}
		return (config);
	}

	private HashMap createConfig(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream fos = new FileInputStream(file);
		String key;
		HashMap map = new HashMap();
		
		Iterator pIt;
		Properties p = new Properties();
		p.load(fos);
		pIt = p.keySet().iterator();
		
		while (pIt.hasNext()) {
			key = (String)pIt.next();
			map.put(key, p.getProperty(key));
		}
		
		return (map);
	}
}
