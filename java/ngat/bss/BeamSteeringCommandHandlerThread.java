/**
 * 
 */
package ngat.bss;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import ngat.bss.command.BEAM_STEERImpl;
import ngat.bss.command.GET_FITSImpl;
import ngat.bss.command.GET_FOCUS_OFFSETImpl;
import ngat.bss.command.GET_STATUSImpl;
import ngat.bss.command.OFFSET_TT_RA_DECImpl;
import ngat.bss.command.OFFSET_TT_XYImpl;
import ngat.bss.command.REBOOTImpl;
import ngat.message.INST_BSS.GET_FITS;
import ngat.message.INST_BSS.GET_FOCUS_OFFSET;
import ngat.message.RCS_BSS.ABORT;
import ngat.message.RCS_BSS.BEAM_STEER;
import ngat.message.RCS_BSS.GET_STATUS;
import ngat.message.RCS_BSS.OFFSET_TT_RA_DEC;
import ngat.message.RCS_BSS.OFFSET_TT_X_Y;
import ngat.message.RCS_BSS.REBOOT;
import ngat.message.base.ACK;
import ngat.message.base.COMMAND;
import ngat.message.base.COMMAND_DONE;
import ngat.util.logging.LogGenerator;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

/**
 * Handles beam steering command
 * 
 * @author eng
 * 
 */
public class BeamSteeringCommandHandlerThread extends Thread {

	public static final int DEFAULT_ACK_TIMEOUT = 10000;

	private BeamSteeringController bss;

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private COMMAND command;
	private Socket socket;

	private LogGenerator logger;

	/**
	 * @param socket
	 */
	public BeamSteeringCommandHandlerThread(BeamSteeringController bss, Socket socket) throws Exception {
		super();
		this.bss = bss;
		this.socket = socket;
		if (socket == null)
			throw new IllegalArgumentException("Null socket");
		Logger alogger = LogManager.getLogger("BSS");
		logger = alogger.generate().system("BSS").subSystem("Comms").srcCompClass("Handler");
	}

	public void run() {

		// open input stream
		try {
			getInputStream();
			logger.create().extractCallInfo().info().level(4)
					.msg("Opened input stream on socket: " + socket.getLocalPort()).send();
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(2)
					.msg("Failed to open input stream for socket: " + socket.getLocalPort()).send();
			return;
		}

		// open output stream
		try {
			getOutputStream();
			logger.create().extractCallInfo().info().level(4)
					.msg("Opened output stream on socket: " + socket.getLocalPort()).send();
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(2)
					.msg("Failed to open output stream for socket: " + socket.getLocalPort()).send();
			return;
		}

		// read command
		try {
			command = getCommandFromClient();
			logger.create().extractCallInfo().info().level(3).msg("Obtained command: " + command.getClass().getName())
					.send();
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(2).msg("Failed to obtain command from input stream: " + e);
			return;
		}

		// create a command-specific handler
		BeamSteeringCommandHandler handler = null;
		try {
			handler = createHandler(command);
			logger.create().extractCallInfo().info().level(3).msg("Created handler: " + handler).send();
		} catch (Exception e) {
			logger.create().extractCallInfo().error().level(2).msg("Failed to create handler: " + e).send();
			// send error based on exception
			COMMAND_DONE done = new COMMAND_DONE(getName());
			done.setSuccessful(false);
			done.setErrorNum(2);
			done.setErrorString("Exception: " + e);
			try {
				sendDone(done);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			return;
		}

		// send first ACK
		long initAckTimeout = handler.getInitialHandlingTime();

		ACK ack = new ACK(command.getId());
		ack.setTimeToComplete((int)initAckTimeout);
		try {
			sendAcknowledge(ack);
		} catch (Exception e) {
			e.printStackTrace();
			logger.create().extractCallInfo().error().level(2)
				.msg("Sending initial ack: " + e).send();
			// no point sending an error back to client
			return;
		}

		// process the command
		try {
			COMMAND_DONE done = handler.processCommand();
			logger.create().extractCallInfo().info().level(3).msg("Command processing completed").send();
			try {
				sendDone(done);
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.create().extractCallInfo().error().level(2).msg("Failed to process command: " + e).send();
			// send an error back to client
			COMMAND_DONE done = new COMMAND_DONE(getName());
			done.setSuccessful(false);
			done.setErrorNum(3);
			done.setErrorString("Exception: " + e);
			try {
				sendDone(done);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			return;
		}
		
		try {		
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.create().extractCallInfo().error().level(2)
				.msg("Failed to close socket "+e)
				.send();
			
		}

	}

	private void getInputStream() throws Exception {
		in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
	}

	/**
	 * Opens up the Object Output stream <a
	 * href="#outputStream">outputStream</a> from Socket <a
	 * href="#socket">socket</a> so that we can send objects to the client. If
	 * the socket is null or an IOException occurs the outputStream is null.
	 * This method should not be overridden.
	 * 
	 * @exception IOException
	 *                If the ObjectOutputStream fails to be constructed an
	 *                IOException results.
	 * @see #run
	 */
	private void getOutputStream() throws Exception {
		out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		out.flush();
	}

	/**
	 * logger.create().extractCallInfo().error().level(2) .msg("Failed to This
	 * routine tries to get the <a href="#command">command</a> the client has
	 * sent by reading an object from the socket. If the <a
	 * href="#inputStream">inputStream</a> is null or an exception occurs the <a
	 * href="#command">command</a> is null.
	 * 
	 * @exception IOException
	 *                If the readObject fails to be constructed an IOException
	 *                results.
	 * @exception ClassNotFoundException
	 *                If the readObject reads an unknown class a
	 *                ClassNotFoundException results.
	 * @see #command
	 * @see #run
	 */
	private COMMAND getCommandFromClient() throws Exception {

		// socket.setSoTimeout(TCPConstants.getCommandTimeOut());

		COMMAND command = (COMMAND) (in.readObject());

		String commandClassName = null;
		if (command == null)
			throw new Exception("Null command");
		else
			commandClassName = command.getClass().getName();

		// set name to include command we are processing
		setName(new String(this.getClass().getName() + ":command:" + commandClassName + ":remote:"
				+ socket.getInetAddress().getHostName() + ":" + socket.getPort() + ":local:"
				+ socket.getLocalAddress().getHostName() + ":" + socket.getLocalPort()));

		return command;
	}

	/**
	 * This routine examines the <a href="#command">command</a> the client has
	 * sent. It is called as soon as the command object is read from the client.
	 * This allows the server to perform any initialisation before the
	 * acknowledge time is calculated.
	 */
	private void init() {
	}

	/**
	 * This routine examines the <a href="#command">command</a> the client has
	 * sent and estimates how long it will take to perform operations relevant
	 * to this command. It returns an instance of ngat.message.base.ACK
	 * containing the time in milliseconds to complete this command. . This
	 * method should be overridden in subclasses to calculate this time
	 * accurately. This method just returns an ACK with 1000 milliseconds.
	 * 
	 * @return A instance of (a subclass of) ngat.message.base.ACK, containing
	 *         the time taken to complete the command in milliseconds.
	 * @see #command
	 * @see #run
	 * @see ngat.message.base.ACK
	 */
	private ACK createInitialAck() {
		ACK acknowledge = null;

		acknowledge = new ACK(command.getId());
		acknowledge.setTimeToComplete(DEFAULT_ACK_TIMEOUT);
		return acknowledge;
	}

	/**
	 * This routine sends an acknowledge back to the client to tell it the
	 * command has been received.
	 * 
	 * @param acknowledge
	 *            The acknowledge object to send back to the client.
	 * @exception If
	 *                the acknowledge object fails to be sent an IOException
	 *                results.
	 * @see #run
	 */
	private void sendAcknowledge(ACK acknowledge) throws Exception {
		if (acknowledge == null) {
			throw new NullPointerException(this.getClass().getName() + "sendAcknowledge:acknowledge was NULL.");
		}
		out.writeObject(acknowledge);
		out.flush();
	}

	/**
	 * This method writes the <a href="#done">done</a> object to the <a
	 * href="#outputStream">outputStream</a> to inform the client the command
	 * has been completed. If the <a href="#done">done</a> object is null it is
	 * created as a error message.
	 * 
	 * @exception If
	 *                the done object fails to be sent an IOException results.
	 * @see #run
	 */
	private void sendDone(COMMAND_DONE done) throws Exception {
		if (done == null) {
			done = new COMMAND_DONE(command.getId());
			done.setErrorNum(1);
			done.setErrorString(getName() + ":sendDone:done was NULL");
			done.setSuccessful(false);
		}
		out.writeObject(done);
		out.flush();
	}

	/**
	 * This routine examines the <a href="#command">command</a> the client has
	 * sent and executes the operations relevant to this command. This method
	 * should be overridden in subclasses to call the routines to perform the
	 * execution.
	 * 
	 * @see #run
	 */

	private BeamSteeringCommandHandler createHandler(COMMAND command) throws Exception {

		// based on specific command, create a handler

		if (command instanceof GET_STATUS) {
			return new GET_STATUSImpl(bss, this, command);
		} else if (command instanceof GET_FOCUS_OFFSET) {
			return new GET_FOCUS_OFFSETImpl(bss, this, command);
		} else if (command instanceof GET_FITS) {
			return new GET_FITSImpl(bss, this, command);
		} else if (command instanceof BEAM_STEER) {
			return new BEAM_STEERImpl(bss, this, command);
		} else if (command instanceof OFFSET_TT_RA_DEC) {
			return new OFFSET_TT_RA_DECImpl(bss, this, command);
		} else if (command instanceof OFFSET_TT_X_Y) {
			return new OFFSET_TT_XYImpl(bss, this, command);
		} else if (command instanceof REBOOT) {
			return new REBOOTImpl(bss, this, command);
		} else if (command instanceof ABORT) {
			//return new ABORTImpl();
		}

		throw new Exception("No handler available for: " + command.getClass().getName());
	}

}
