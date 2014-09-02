/**
 * 
 */
package ngat.bss.test;

import ngat.message.INST_BSS.GET_FOCUS_OFFSET;
import ngat.message.RCS_BSS.BEAM_STEER;
import ngat.message.base.COMMAND;

/**
 * @author eng
 *
 */
public class SendNullCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			SendCommand send = new SendCommand("localhost", 6789);
			COMMAND command = new COMMAND("test");
			
			send.sendCommand(command);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
