/**
 * 
 */
package ngat.bss.test;

import ngat.message.RCS_BSS.OFFSET_TT_RA_DEC;

/**
 * @author eng
 *
 */
public class SendOffsetRadecCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		OFFSET_TT_RA_DEC command = null;
		
		if (args == null || args.length == 0) {
		
			// setup defaults
			
			
			command = new OFFSET_TT_RA_DEC("test");
			command.setRa(Math.random()*Math.PI*2.0);
			command.setDec((Math.random()-0.5)*Math.PI);
			
			command.setRaOffset((float)((Math.random()-0.5)*0.1));
			command.setDecOffset((float)((Math.random()-0.5)*0.1));
			
			command.setInstrumentName("IO:O");
			command.setRotSkyPA(Math.random()*Math.PI*2.0);
			command.setTiptiltId(OFFSET_TT_RA_DEC.TIPTILT_ID_BOTTOM);
			
		} else {
			
			String inst = args[0];
			double ra = Double.parseDouble(args[1]);
			double dec = Double.parseDouble(args[2]);
			double dra = Double.parseDouble(args[3]);
			double ddec = Double.parseDouble(args[4]);
			double rot = Double.parseDouble(args[5]);
			
			command = new OFFSET_TT_RA_DEC("test");
			command.setRa(ra);
			command.setDec(dec);		
			command.setRaOffset((float)dra);
			command.setDecOffset((float)ddec);
			command.setInstrumentName(inst);	
			command.setRotSkyPA(rot);
			command.setTiptiltId(OFFSET_TT_RA_DEC.TIPTILT_ID_BOTTOM);
		}
		
		try {
			
			SendCommand sender = new SendCommand("localhost", 6683);			
			sender.sendCommand(command);			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
