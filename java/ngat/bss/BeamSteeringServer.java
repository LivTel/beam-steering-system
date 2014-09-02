/**
 * 
 */
package ngat.bss;


import java.net.ServerSocket;
import java.net.Socket;


import ngat.util.ControlThread;
import ngat.util.logging.LogGenerator;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

/**
 * @author eng
 *
 */
public class BeamSteeringServer extends ControlThread {

	private int port;
	
	/** Controller.*/
	private BeamSteeringController bss;
	
	/** Server socket.*/
	private ServerSocket server;
	
	/** Logging.*/
	private LogGenerator logger;
	
	
	/**
	 * @param port
	 */
	public BeamSteeringServer(BeamSteeringController bss, int port) throws Exception {
		super("BSS:"+port, true);
		this.bss = bss;
		this.port = port;
		Logger alogger = LogManager.getLogger("BSS");
		logger = alogger.generate().system("BSS").subSystem("Comms").srcCompClass("Server");
	}

	/* (non-Javadoc)
	 * @see ngat.util.ControlThread#initialise()
	 */
	@Override
	protected void initialise() {
		try {
			server =  new ServerSocket(port);	
			logger.create().extractCallInfo().info().level(1)
			.msg("BSS Server bound to port: "+port).send();
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(2)
			.msg("Failed to bind BSS server: "+e);
			terminate();
		}
	}

	/* (non-Javadoc)
	 * @see ngat.util.ControlThread#mainTask()
	 */
	@Override
	protected void mainTask() {
		
		// loop until stopped, creating sockets
		Socket socket = null;
		try {
			socket = server.accept();
			// pass the socket to a handler
			BeamSteeringCommandHandlerThread bssHandler = new BeamSteeringCommandHandlerThread(bss, socket);
			bssHandler.start();
		} catch (Exception e) {
			
		}
	}

	/* (non-Javadoc)
	 * @see ngat.util.ControlThread#shutdown()
	 */
	@Override
	protected void shutdown() {
		// TODO Auto-generated method stub

	}

}
