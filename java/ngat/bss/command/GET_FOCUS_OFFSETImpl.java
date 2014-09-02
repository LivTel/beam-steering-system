/**
 * 
 */
package ngat.bss.command;

import ngat.bss.BeamSteeringCommandHandler;
import ngat.bss.BeamSteeringCommandHandlerThread;
import ngat.bss.BeamSteeringController;
import ngat.message.INST_BSS.GET_FOCUS_OFFSET;
import ngat.message.INST_BSS.GET_FOCUS_OFFSET_DONE;
import ngat.message.base.COMMAND;
import ngat.message.base.COMMAND_DONE;

/**
 * @author eng
 *
 */
public class GET_FOCUS_OFFSETImpl extends BeamSteeringCommandHandler {

	/**
	 * @param bss
	 * @param controller
	 * @param command
	 */
	public GET_FOCUS_OFFSETImpl(BeamSteeringController bss, BeamSteeringCommandHandlerThread controller, COMMAND command) {
		super(bss, controller, command);
		
	}

	/* (non-Javadoc)
	 * @see ngat.bss.BeamSteeringCommandHandler#processCommand()
	 */
	@Override
	protected COMMAND_DONE processCommand() throws Exception {
		
		GET_FOCUS_OFFSET_DONE done = new GET_FOCUS_OFFSET_DONE(command.getId());
		
		if (bss.isFullSimulation()) {
			done.setSuccessful(true);
			done.setFocusOffset(0.0f);
			return done;
		}
		
		GET_FOCUS_OFFSET getfocus = (GET_FOCUS_OFFSET)command;
	
		String instrumentName = getfocus.getInstrumentName();
		
		double dfocus = 0.0;
		try {
			dfocus = bss.getFocusOffset(instrumentName);
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(1)
				.msg("Error getting focus offset: "+e.getMessage())
				.send();
			done.setSuccessful(false);
			done.setErrorNum(1);
			done.setErrorString("Error getting focus offset: "+e.getMessage());
			return done;
		}
		
		done.setSuccessful(true);
		done.setFocusOffset((float)dfocus);
		
		return done;
		
	}

	@Override
	protected long getInitialHandlingTime() {
		return 10000L;
	}

}
