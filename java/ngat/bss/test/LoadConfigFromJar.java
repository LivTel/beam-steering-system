/**
 * 
 */
package ngat.bss.test;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author eng
 *
 */
public class LoadConfigFromJar {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			
			LoadConfigFromJar l = new LoadConfigFromJar();
			l.loadProperties();			
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void loadProperties() throws Exception {
		Properties configProp = new Properties();
		InputStream in = this.getClass().getResourceAsStream("/ngat/bss/config/bss.properties");
		configProp.load(in);
		
		System.err.println("Config: "+configProp);
		
	}

}
