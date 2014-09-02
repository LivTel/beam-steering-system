/**
 * 
 */
package ngat.bss.command;

import java.util.Vector;

import ngat.bss.BeamSteeringCommandHandler;
import ngat.bss.BeamSteeringCommandHandlerThread;
import ngat.bss.BeamSteeringController;
import ngat.fits.FitsHeaderCardImage;
import ngat.message.INST_BSS.GET_FITS;
import ngat.message.INST_BSS.GET_FITS_DONE;
import ngat.message.base.COMMAND;
import ngat.message.base.COMMAND_DONE;

/**
 * @author eng
 *
 */
public class GET_FITSImpl extends BeamSteeringCommandHandler {

	/**
	 * @param bss
	 * @param controller
	 * @param command
	 */
	public GET_FITSImpl(BeamSteeringController bss, BeamSteeringCommandHandlerThread controller, COMMAND command) {
		super(bss, controller, command);
		
	}

	/* (non-Javadoc)
	 * @see ngat.bss.BeamSteeringCommandHandler#processCommand()
	 */
	@Override
	protected COMMAND_DONE processCommand() throws Exception {

		GET_FITS_DONE done = new GET_FITS_DONE(command.getId());
				
		GET_FITS getfits = (GET_FITS)command;
		
		String instrumentName = getfits.getInstrumentName();
		
		Vector fits = null;
		try {
			fits = bss.getFits(instrumentName);
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(1)
			.msg("Error getting fits: "+e.getMessage())
			.send();
			done.setSuccessful(false);
			done.setErrorNum(1);
			done.setErrorString("Error getting fits: "+e.getMessage());
			return done;
		}
		
		done.setSuccessful(true);
		done.setFitsHeader(fits);
		
		return done;
		
	}

	@Override
	protected long getInitialHandlingTime() {
		return 10000L;
	}

}
