/**
 * 
 */
package ngat.bss.test;

import ngat.dichroic.DichroicException;
import ngat.dichroic.DichroicMoveException;
import ngat.message.INST_BSS.GET_FOCUS_OFFSET;
import ngat.message.INST_BSS.GET_FOCUS_OFFSET_DONE;
import ngat.message.base.COMMAND_DONE;

/** Send a GET_FOCUS_OFFSET command
 * @author eng
 *
 */
public class SendGetFocusCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		
			String instrumentName = args[0];
			
			GET_FOCUS_OFFSET getfocus = new GET_FOCUS_OFFSET("test");
			getfocus.setInstrumentName(instrumentName);
			
			SendCommand sender = new SendCommand("localhost", 6683);			
			COMMAND_DONE done = sender.sendCommand(getfocus);		
			
			if (done instanceof GET_FOCUS_OFFSET_DONE) {
				
				GET_FOCUS_OFFSET_DONE reply = (GET_FOCUS_OFFSET_DONE)done;
				
				double dfocus = (double)reply.getFocusOffset();
				
				System.err.println("Focus offset for: "+instrumentName+" = "+dfocus);			
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
