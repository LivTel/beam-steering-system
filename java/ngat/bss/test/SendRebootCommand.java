/**
 * 
 */
package ngat.bss.test;

import ngat.message.RCS_BSS.REBOOT;
import ngat.message.RCS_BSS.REBOOT_DONE;
import ngat.message.base.COMMAND_DONE;

/**
 * @author eng
 *
 */
public class SendRebootCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			
			REBOOT reboot = new REBOOT("test");
			
			int level = Integer.parseInt(args[0]);
			reboot.setLevel(level);
			
			SendCommand sender = new SendCommand("localhost", 6683);			
			COMMAND_DONE done = sender.sendCommand(reboot);		
			
			if (done instanceof REBOOT_DONE) {
				REBOOT_DONE gsdone= (REBOOT_DONE)done;				
				System.err.println("Received reboot confirmation");
			
				
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
