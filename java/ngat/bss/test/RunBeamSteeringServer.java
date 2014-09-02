/**
 * 
 */
package ngat.bss.test;

import ngat.bss.BeamSteeringServer;
import ngat.util.logging.BasicLogFormatter;
import ngat.util.logging.ConsoleLogHandler;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

/**
 * @author eng
 *
 */
public class RunBeamSteeringServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Logger logger = LogManager.getLogger("BSS");
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(126));
		console.setLogLevel(5);
		logger.setLogLevel(5);
		//logger.addHandler(console);
		logger.addExtendedHandler(console);
		try {
			
			BeamSteeringServer server = new BeamSteeringServer(null, 6789);
			server.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
