/**
 * 
 */
package ngat.bss.test;

import ngat.dichroic.Dichroic;
import ngat.message.RCS_BSS.BEAM_STEER;
import ngat.phase2.XBeamSteeringConfig;
import ngat.phase2.XOpticalSlideConfig;

/**
 * @author eng
 *
 */
public class SendBeamSteerCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		BEAM_STEER command = null;
		
		if (args == null) {
		
			// setup defaults
			XOpticalSlideConfig slideConfig1 = new XOpticalSlideConfig(XOpticalSlideConfig.SLIDE_UPPER);
			slideConfig1.setElementName("Clear");
			XOpticalSlideConfig slideConfig2 = new XOpticalSlideConfig(XOpticalSlideConfig.SLIDE_LOWER);
			slideConfig2.setElementName("Di-RB6700");
			
			XBeamSteeringConfig beam = new XBeamSteeringConfig(slideConfig1, slideConfig2);
			
			command = new BEAM_STEER("test");
			command.setBeamConfig(beam);
		} else {
			
			String uslide = args[0];
			String lslide = args[1];
		
			// decide on the light path from args
			XOpticalSlideConfig slideConfig1 = new XOpticalSlideConfig(XOpticalSlideConfig.SLIDE_UPPER);
			slideConfig1.setElementName(uslide);
			XOpticalSlideConfig slideConfig2 = new XOpticalSlideConfig(XOpticalSlideConfig.SLIDE_LOWER);
			slideConfig2.setElementName(lslide);
			
			XBeamSteeringConfig beam = new XBeamSteeringConfig(slideConfig1, slideConfig2);
			
			command = new BEAM_STEER("test");
			command.setBeamConfig(beam);
			
		}
		
		try {
			
			SendCommand sender = new SendCommand("localhost", 6683);			
			sender.sendCommand(command);			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
