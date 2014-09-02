/**
 * 
 */
package ngat.bss.test;

import java.util.Enumeration;
import java.util.Hashtable;

import ngat.message.RCS_BSS.GET_STATUS;
import ngat.message.RCS_BSS.GET_STATUS_DONE;
import ngat.message.base.COMMAND_DONE;

/**
 * @author eng
 *
 */
public class SendGetStatusCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		
		try {
			
			GET_STATUS getstatus = new GET_STATUS("test");
			
			SendCommand sender = new SendCommand("localhost", 6683);			
			COMMAND_DONE done = sender.sendCommand(getstatus);		
			
			if (done instanceof GET_STATUS_DONE) {
				GET_STATUS_DONE gsdone= (GET_STATUS_DONE)done;				
				System.err.println("Received status:-");
				
				Hashtable hash = gsdone.getStatusData();
				
				Enumeration e = hash.keys();
				while (e.hasMoreElements()) {
					Object key = e.nextElement();
					Object value = hash.get(key);
					System.err.printf("%30.30s : %30.30s \n", key, value);
				}
				
				
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
