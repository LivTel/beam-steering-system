/**
 * 
 */
package ngat.bss.test;

import ngat.message.RCS_BSS.OFFSET_TT_X_Y;

/**
 * @author eng
 *
 */
public class SendOffsetXYCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		OFFSET_TT_X_Y command = null;
		
		if (args == null || args.length == 0) {
		
			// setup defaults
			
			
			command = new OFFSET_TT_X_Y("test");
			command.setXOffset((float)(Math.random()*Math.PI*2.0));
			command.setYOffset( (float)((Math.random()-0.5)*Math.PI));
			
			command.setInstrumentName("IO:O");
		
			command.setTiptiltId(OFFSET_TT_X_Y.TIPTILT_ID_BOTTOM);
			
		} else {
			
			String inst = args[0];
		
			double dx = Double.parseDouble(args[1]);
			double dy = Double.parseDouble(args[2]);
		
			command = new OFFSET_TT_X_Y("test");
			
			command.setXOffset((float)dx);
			command.setYOffset((float)dy);
			command.setInstrumentName(inst);	
		
			command.setTiptiltId(OFFSET_TT_X_Y.TIPTILT_ID_BOTTOM);
		}
		
		try {
			
			SendCommand sender = new SendCommand("localhost", 6683);			
			sender.sendCommand(command);			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
