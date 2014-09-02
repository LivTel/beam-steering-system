/**
 * 
 */
package ngat.bss;

import ngat.message.base.COMMAND;
import ngat.message.base.COMMAND_DONE;
import ngat.util.logging.LogGenerator;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

/** Subclass to handle any specific beam-steering command.
 * @author eng
 *
 */
public abstract class BeamSteeringCommandHandler {

	protected BeamSteeringController bss;
	
	/** The controller through which we send responses.*/
	protected BeamSteeringCommandHandlerThread controller;
	
	protected LogGenerator logger;
	
	/** The command to process.*/
	protected COMMAND command;

	/** Create a BeamSteeringCommandHandler
	 * @param controller The controller through which we send responses.
	 * @param command The command to process.
	 */
	// TODO add BSS controller as param, all handlers will need access to this chappy
	public BeamSteeringCommandHandler(BeamSteeringController bss, BeamSteeringCommandHandlerThread controller, COMMAND command) {
		super();
		this.bss = bss;
		this.controller = controller;
		this.command = command;
		Logger alogger = LogManager.getLogger("BSS");
		logger = alogger.generate().system("BSS").subSystem("Comms").srcCompClass("Handler").srcCompId(command.getId());
	}
	
	/** Process the command, may have to send various acks.*/
	protected abstract COMMAND_DONE processCommand() throws Exception;
	
	/** @return The initial guess at handling time.*/
	protected abstract long getInitialHandlingTime();
	
}
