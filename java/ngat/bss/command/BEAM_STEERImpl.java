/**
 * 
 */
package ngat.bss.command;

import ngat.bss.BeamSteeringCommandHandler;
import ngat.bss.BeamSteeringCommandHandlerThread;
import ngat.bss.BeamSteeringController;
import ngat.message.RCS_BSS.BEAM_STEER;
import ngat.message.RCS_BSS.BEAM_STEER_DONE;
import ngat.message.base.COMMAND;
import ngat.message.base.COMMAND_DONE;
import ngat.phase2.IOpticalSlideConfig;
import ngat.phase2.XBeamSteeringConfig;
import ngat.phase2.XOpticalSlideConfig;

/**
 * @author eng
 *
 */
public class BEAM_STEERImpl extends BeamSteeringCommandHandler {

	
	
	/** Create a BEAM_STEERImpl.
	 * @param bss
	 * @param controller
	 * @param command
	 */
	public BEAM_STEERImpl(BeamSteeringController bss, BeamSteeringCommandHandlerThread controller, COMMAND command) {
		super(bss, controller, command);
		
	}

	/* (non-Javadoc)
	 * @see ngat.bss.BeamSteeringCommandHandler#processCommand()
	 */
	@Override
	protected COMMAND_DONE processCommand() throws Exception {
		
		BEAM_STEER_DONE done = new BEAM_STEER_DONE(command.getId());
		
		// full simulation
		if (bss.isFullSimulation()) {
			done.setSuccessful(true);
			return done;
		}
		
		BEAM_STEER beam = (BEAM_STEER)command;
		
		XBeamSteeringConfig config = beam.getBeamConfig();
		if (config == null) {
			done.setSuccessful(false);
			done.setErrorNum(1);
			done.setErrorString("Beam config is NULL");
			return done;
		}
		
		try {
			XOpticalSlideConfig upperSlideConfig = config.getUpperSlideConfig();
			logger.create().extractCallInfo().info().level(3)
				.msg("Processing request for upper slide: Requested position: "+				
						upperSlideConfig.getElementName())
				.send();
			
			// check valid position			
			if (!bss.hasUpperElement(upperSlideConfig.getElementName())) {
				done.setSuccessful(false);
				done.setErrorNum(1);
				done.setErrorString("Illegal upper slide position: "+upperSlideConfig.getElementName());
				return done;
			}
			logger.create().extractCallInfo().info().level(3)
			.msg("Calling move upper...").send();
			
			bss.moveUpperSlide(upperSlideConfig);
			logger.create().extractCallInfo().info().level(3)
				.msg("Upper slide moved successfully to: "+upperSlideConfig.getElementName())
				.send();
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(1)
				.msg("Error moving upper slide: "+e)
				.send();
			done.setSuccessful(false);
			done.setErrorNum(1);
			done.setErrorString("Error moving upper slide: "+e.getMessage());
			return done;
		}
		
		try {
			XOpticalSlideConfig lowerSlideConfig = config.getLowerSlideConfig();
			logger.create().extractCallInfo().info().level(3)
			.msg("Processing request for lower slide: Requested position: "+
					lowerSlideConfig.getElementName())
			.send();
			
			// check valid position
			if (!bss.hasLowerElement(lowerSlideConfig.getElementName())) {
				done.setSuccessful(false);
				done.setErrorNum(1);
				done.setErrorString("Illegal lower slide position: "+lowerSlideConfig.getElementName());
				return done;
			}
			
			bss.moveLowerSlide(lowerSlideConfig);
			logger.create().extractCallInfo().info().level(3)
				.msg("Lower slide moved successfully to: "+lowerSlideConfig.getElementName())
				.send();
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(1)
				.msg("Error moving lower slide: "+e)
				.send();
			done.setSuccessful(false);
			done.setErrorNum(1);
			done.setErrorString("Error moving lower slide: "+e.getMessage());
			return done;
		}
		
		// check where the buggers ended up.
		XOpticalSlideConfig actualUpper = bss.getUpperSlide();
		XOpticalSlideConfig actualLower = bss.getLowerSlide();
						
		done.setSuccessful(true);
		return done;
		
	}

	@Override
	protected long getInitialHandlingTime() {
		return 90000L;
	}

}
