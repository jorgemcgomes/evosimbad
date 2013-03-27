package org.neat4j.core.distribute;
import java.net.MalformedURLException;

public class RemoteExperimentAgentClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = args[0];
		ExperimentAgent eh;
		int numAgents ;
		int i;
		try {
			if (args.length > 1) {
				numAgents = Integer.parseInt(args[1]);
			} else {
				numAgents = 1;
			}
			
			for (i = 0; i < numAgents; i++) {
				eh = new ExperimentAgent(url);
				new Thread(eh).start();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
