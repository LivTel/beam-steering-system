/**
 * 
 */
package ngat.bss.command;

import java.net.Socket;

import ngat.bss.BeamSteeringCommandHandler;
import ngat.bss.BeamSteeringCommandHandlerThread;
import ngat.bss.BeamSteeringController;
import ngat.message.RCS_BSS.OFFSET_TT_X_Y;
import ngat.message.RCS_BSS.OFFSET_TT_X_Y_DONE;
import ngat.message.base.COMMAND;
import ngat.message.base.COMMAND_DONE;

/**
 * @author eng
 *
 */
public class OFFSET_TT_XYImpl extends BeamSteeringCommandHandler {

	

	/** Create a OFFSET_TT_XYImpl.
	 * @param bss
	 * @param controller
	 * @param command
	 */
	public OFFSET_TT_XYImpl(BeamSteeringController bss, BeamSteeringCommandHandlerThread controller, COMMAND command) {
		super(bss, controller, command);
	
	}

	@Override
	protected COMMAND_DONE processCommand() throws Exception {
	
		OFFSET_TT_X_Y_DONE done = new OFFSET_TT_X_Y_DONE(command.getId());
		
		OFFSET_TT_X_Y offset = (OFFSET_TT_X_Y)command;
		
		// which mechanism
		int mechanism = offset.getTiptiltId();
		
		double xOffset  = (double)offset.getXOffset();
		double yOffset = (double)offset.getYOffset();
		
		// instrument plane corrections
		String instrumentName = offset.getInstrumentName();
		
		try {
			switch (mechanism) {
			case OFFSET_TT_X_Y.TIPTILT_ID_BOTTOM:		
				bss.offsetLowerTiptiltXY(xOffset,yOffset, instrumentName);
				break;
			case OFFSET_TT_X_Y.TIPTILT_ID_TOP:
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
