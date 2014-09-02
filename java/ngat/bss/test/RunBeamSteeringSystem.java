/**
 * 
 */
package ngat.bss.test;

import java.io.File;

import ngat.bss.BeamSteeringController;
import ngat.phase2.IOpticalSlideConfig;
import ngat.util.ConfigurationProperties;
import ngat.util.PropertiesConfigurator;
import ngat.util.logging.BasicLogFormatter;
import ngat.util.logging.ConsoleLogHandler;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

/**
 * @author eng
 *
 */
public class RunBeamSteeringSystem {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		Logger logger = LogManager.getLogger("BSS");
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(126));
		console.setLogLevel(5);
		logger.setLogLevel(5);
	
		logger.addExtendedHandler(console);
		try {
			String configFileName = args[0];
			File file = new File(configFileName);
			
			BeamSteeringController bss = new BeamSteeringController();
			
			PropertiesConfigurator.use(file).configure(bss);
		
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
