/**
 * 
 */
package ngat.bss.command;

import ngat.bss.BeamSteeringCommandHandler;
import ngat.bss.BeamSteeringCommandHandlerThread;
import ngat.bss.BeamSteeringController;
import ngat.message.RCS_BSS.OFFSET_TT_RA_DEC;
import ngat.message.RCS_BSS.OFFSET_TT_RA_DEC_DONE;
import ngat.message.base.COMMAND;
import ngat.message.base.COMMAND_DONE;
import ngat.phase2.XOpticalSlideConfig;

/**
 * @author eng
 *
 */
public class OFFSET_TT_RA_DECImpl extends BeamSteeringCommandHandler {

	
	
	/** Create a OFFSET_TT_RA_DECImpl.
	 * @param bss
	 * @param controller
	 * @param command
	 */
	public OFFSET_TT_RA_DECImpl(BeamSteeringController bss, BeamSteeringCommandHandlerThread controller, COMMAND command) {
		super(bss, controller, command);
	
	}

	/* (non-Javadoc)
	 * @see ngat.bss.BeamSteeringCommandHandler#processCommand()
	 */
	@Override
	protected COMMAND_DONE processCommand() throws Exception {
		
		OFFSET_TT_RA_DEC_DONE done = new OFFSET_TT_RA_DEC_DONE(command.getId());
		
		OFFSET_TT_RA_DEC offset = (OFFSET_TT_RA_DEC)command;
		
		// which mechanism
		int mechanism = offset.getTiptiltId();
		
		double raOffset  = (double)offset.getRaOffset();
		double decOffset = (double)offset.getDecOffset();
		
		// instrument plane corrections
		String instrumentName = offset.getInstrumentName();
		
		// instrument rotator correction
		double rotatorCorrection = offset.getRotSkyPA();
		
		// target position on sky
		double ra  = offset.getRa();
		double dec = offset.getDec();
		
		try {
			switch (mechanism) {
			case OFFSET_TT_RA_DEC.TIPTILT_ID_BOTTOM:			
				bss.offsetLowerTiptiltRaDec(raOffset, decOffset, instrumentName, rotatorCorrection, ra, dec);
				break;
			case OFFSET_TT_RA_DEC.TIPTILT_ID_TOP:
				done.setSuccessful(false);
				done.setErrorNum(1);
				done.setErrorString("Upper tiptilt is not available");
				return done;
			}
			
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(1)
			.msg("Error moving tiptilt: "+e)
			.send();
			done.setSuccessful(false);
			done.setErrorNum(1);
			done.setErrorString("Error moving tiptilt: "+e);
			return done;
		}
		
		done.setSuccessful(true);
		return done;
		
	}

	@Override
	protected long getInitialHandlingTime() {
		return 60000L;
	}

}
