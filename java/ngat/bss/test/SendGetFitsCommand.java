/**
 * 
 */
package ngat.bss.test;

import java.util.Iterator;
import java.util.Vector;

import ngat.fits.FitsHeaderCardImage;
import ngat.message.INST_BSS.GET_FITS;
import ngat.message.INST_BSS.GET_FITS_DONE;
import ngat.message.base.COMMAND_DONE;

/**
 * @author eng
 *
 */
public class SendGetFitsCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		try {
			
			String instrumentName = args[0];
			
			GET_FITS getfits = new GET_FITS("test");
			getfits.setInstrumentName(instrumentName);
			
			SendCommand sender = new SendCommand("ltsim1", 6683);			
			COMMAND_DONE done = sender.sendCommand(getfits);		
			
			if (done instanceof GET_FITS_DONE) {
				
				GET_FITS_DONE reply = (GET_FITS_DONE)done;
			
				Vector fits = reply.getFitsHeader();
				
				for (int il = 0; il < fits.size(); il++) {
					
					FitsHeaderCardImage f = (FitsHeaderCardImage)fits.get(il);
					System.err.println(""+f);
					
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
