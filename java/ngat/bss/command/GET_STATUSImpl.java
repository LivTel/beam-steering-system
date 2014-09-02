/**
 * 
 */
package ngat.bss.command;

import java.util.Hashtable;
import java.util.Map;

import ngat.bss.BeamSteeringCommandHandler;
import ngat.bss.BeamSteeringCommandHandlerThread;
import ngat.bss.BeamSteeringController;
import ngat.message.RCS_BSS.GET_STATUS_DONE;
import ngat.message.base.COMMAND;
import ngat.message.base.COMMAND_DONE;

/**
 * @author eng
 *
 */
public class GET_STATUSImpl extends BeamSteeringCommandHandler {
	
	
	/** Create a GET_STATUSImpl.
	 * @param bss 
	 * @param controller
	 * @param command
	 */
	public GET_STATUSImpl(BeamSteeringController bss, BeamSteeringCommandHandlerThread controller, COMMAND command) {
		super(bss, controller, command);
	}
	
	/* (non-Javadoc)
	 * @see ngat.bss.BeamSteeringCommandHandler#processCommand()
	 */
	@Override
	protected COMMAND_DONE processCommand() throws Exception {
		
		Map map = bss.getStatus();
		
		Hashtable hash = new Hashtable(map);
		
		GET_STATUS_DONE done = new GET_STATUS_DONE(command.getId());
		done.setSuccessful(true);
		done.setStatusData(hash);
			
		return done;
		
	}

	@Override
	protected long getInitialHandlingTime() {
		return 10000L;
	}

}
