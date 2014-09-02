/**
 * 
 */
package ngat.bss.command;

import ngat.bss.BeamSteeringCommandHandler;
import ngat.bss.BeamSteeringCommandHandlerThread;
import ngat.bss.BeamSteeringController;
import ngat.message.RCS_BSS.REBOOT;
import ngat.message.RCS_BSS.REBOOT_DONE;
import ngat.message.base.COMMAND;
import ngat.message.base.COMMAND_DONE;

/**
 * @author eng
 *
 */
public class REBOOTImpl extends BeamSteeringCommandHandler {

	public REBOOTImpl(BeamSteeringController bss, BeamSteeringCommandHandlerThread controller, COMMAND command) {
		super(bss, controller, command);
		
	}

	@Override
	protected COMMAND_DONE processCommand() throws Exception {
		
		REBOOT_DONE done = new REBOOT_DONE(command.getId());
		
		REBOOT reboot = (REBOOT)command;
		
		int level = reboot.getLevel();
		logger.create().extractCallInfo().info().level(3)
		.msg("Reboot requested at level: "+level).send();
		
		switch (level){
		case REBOOT.LEVEL_REDATUM:
			try {
				bss.postConfigure();
			} catch (Exception e) {
				logger.create().extractCallInfo().error().level(1)
				.msg("Error re-configuring BSS controller: "+e)
				.send();
				done.setSuccessful(false);
				done.setErrorNum(1);
				done.setErrorString("Error re-configuring BSS controller: "+e);
				return done;
			}
			break;
		case REBOOT.LEVEL_SOFTWARE:
			try {
				bss.restart();
			} catch (Exception e) {
				logger.create().extractCallInfo().error().level(1)
				.msg("Error re-starting BSS controller: "+e)
				.send();
				done.setSuccessful(false);
				done.setErrorNum(1);
				done.setErrorString("Error re-starting BSS controller: "+e);
				return done;
			}
			break;
		case REBOOT.LEVEL_POWER_OFF:
			try {
				bss.shutdown();
			} catch (Exception e) {
				logger.create().extractCallInfo().error().level(1)
				.msg("Error shutting down BSS controller: "+e)
				.send();
				done.setSuccessful(false);
				done.setErrorNum(1);
				done.setErrorString("Error shutting down BSS controller: "+e);
				return done;
			}
			break;
			default:
				done.setSuccessful(false);
				done.setErrorNum(1);
				done.setErrorString("Reboot level: "+level+" is not implemented for this system");
				return done;
		}
		
		done.setSuccessful(true);
		return done;
	}

	@Override
	protected long getInitialHandlingTime() {
		return 10000L;
	}

}
