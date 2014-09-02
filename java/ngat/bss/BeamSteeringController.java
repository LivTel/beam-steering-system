package ngat.bss;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import sun.net.InetAddressCachePolicy;

import ngat.astrometry.AstroFormatter;
import ngat.dichroic.Dichroic;
import ngat.fits.FitsHeaderCardImage;
import ngat.phase2.IOpticalSlideConfig;
import ngat.phase2.XOpticalSlideConfig;
import ngat.phase2.XTipTiltAbsoluteOffsetConfig;
import ngat.util.ConfigurationProperties;
import ngat.util.PropertiesConfigurable;
import ngat.util.logging.LogGenerator;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

/**
 * Implements beam steering. TODO May add rmi interface later.
 * 
 * @author eng
 * 
 */
public class BeamSteeringController implements PropertiesConfigurable {

	/** Exit state indicating re-start. */
	public static final int EXIT_RESTART = 12;

	/** Exit state indicating shutdown. */
	public static final int EXIT_SHUTDOWN = 13;

	public static final int DEFAULT_SERVER_PORT = 6683;

	private XOpticalSlideConfig upperSlide;

	private XOpticalSlideConfig lowerSlide;

	private int upperSlidePosition;

	private int upperSlideStatus;

	private int lowerSlidePosition;

	private int lowerSlideStatus;

	// private XTipTiltAbsoluteOffsetConfig upperTiptilt;

	private XTipTiltAbsoluteOffsetConfig lowerTiptilt;

	private String upperSlideHostName;

	private int upperSlidePort;

	private String lowerSlideHostName;

	private int lowerSlidePort;

	private double tiptiltXOffset;
	private double tiptiltYOffset;
	// last ra,dec used
	// arduino stuff here

	private double tiptiltRa;
	private double tiptiltDec;
	private double tiptiltRaOffset;
	private double tiptiltDecOffset;
	// last mode (xy or radec)
	private int tiptiltMode;
	// last instrument
	private String tiptiltInstrumentName;
	// last rotator
	private double tiptiltRotSkyPa;

	/** Initial configuration. */
	ConfigurationProperties config;

	ConfigurationProperties focusOffsetMap;

	Map slideFilterNameMap; // map filter.class to (position, focus offset)
	Map<Integer,String> upperSlidePositionNameMap; // map position to filter class
	Map<Integer,String> lowerSlidePositionNameMap; // map position to filter class
	
	private BeamSteeringServer server;

	private BeamStatusCheckThread checker;

	/** Logging. */
	private LogGenerator logger;

	/**
	 * Set true if simulating the comms to the various mechanisms. Only ever set
	 * in config.
	 */
	private volatile boolean commsSimulation;
	
	/**
	 * Set true if simulating the full function of the BSS.
	 */
	private boolean fullSimulation;

	/**
	 * 
	 */
	public BeamSteeringController() {
		super();
		upperSlidePositionNameMap = new HashMap<Integer, String>();
		lowerSlidePositionNameMap = new HashMap<Integer, String>();
		slideFilterNameMap = new HashMap();

		focusOffsetMap = new ConfigurationProperties();

		Logger alogger = LogManager.getLogger("BSS");
		logger = alogger.generate().system("BSS").subSystem("Control").srcCompClass("Controller");

		upperSlide = new XOpticalSlideConfig(XOpticalSlideConfig.SLIDE_UPPER);
		lowerSlide = new XOpticalSlideConfig(XOpticalSlideConfig.SLIDE_LOWER);
		upperSlide.setElementName("Clear");
		lowerSlide.setElementName("Clear");
		upperSlidePosition = 0; // TODO what does this mean ?
		lowerSlidePosition = 0;
		upperSlideStatus = XOpticalSlideConfig.SLIDE_STATUS_IN_POSITION;
		lowerSlideStatus = XOpticalSlideConfig.SLIDE_STATUS_IN_POSITION;

	}

	public void configure(ConfigurationProperties config) throws Exception {
		this.config = config;

		int serverPort = config.getIntValue("server.port", DEFAULT_SERVER_PORT);
		server = new BeamSteeringServer(this, serverPort);
		server.start();

		checker = new BeamStatusCheckThread(this);
		checker.start();

		postConfigure();

	}

	public void postConfigure() throws Exception {
		logger.create().extractCallInfo().info().level(3).msg("Post configuring, clearing any existing mappings...").send();
		
		upperSlidePositionNameMap.clear();
		lowerSlidePositionNameMap.clear();
		slideFilterNameMap.clear();
		focusOffsetMap.clear();
		
		commsSimulation = (config.getProperty("comms.simulation") != null);
		fullSimulation = (config.getProperty("full.simulation") != null);
		
		// upper slide
		upperSlideHostName = config.getProperty("upper.slide.control.host");
		upperSlidePort = config.getIntValue("upper.slide.control.port");

		// slide position to name mapping
		// read any properties with u.s.xxx
		// the value of xxx IS the classname

		// lower slide
		lowerSlideHostName = config.getProperty("lower.slide.control.host");
		lowerSlidePort = config.getIntValue("lower.slide.control.port");

		
		// tiptilt
		// controller host and port filter name map (only one of) etc

		// TODO a cheat, later we want proper variables to hold this stuff
		for (int ip = 0; ip <= 2; ip++) {
			String pkey = "upper.slide.position."+ip;
			String filter = (String)config.get(pkey);
			if (config.containsKey(pkey)) {
				// make a pmap entry			
				logger.create().extractCallInfo().info().level(3)
					.msg("Insert entry upper position: "+ip+" as "+ filter)
					.send();
				upperSlidePositionNameMap.put(ip, filter);
				config.put("upper.slide."+filter+".position", ""+ip);
				slideFilterNameMap.put("upper.slide."+filter+".filter.position", ""+ip);
			}
			pkey = "lower.slide.position."+ip;
			filter = (String)config.get(pkey);
			if (config.containsKey(pkey)) {
				// make a pmap entry	
				logger.create().extractCallInfo().info().level(3)
				.msg("Insert entry lower position: "+ip+" as "+ filter)
				.send();
				lowerSlidePositionNameMap.put(ip, filter);
				config.put("lower.slide."+filter+".position", ""+ip);
				slideFilterNameMap.put("lower.slide."+filter+".filter.position", ""+ip);
			}
		}
	
		slideFilterNameMap.putAll(config);
		focusOffsetMap.putAll(config);

	}

	public void restart() {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.create().extractCallInfo().info().level(3).msg("Restarting BSS controller in 5 sec...")
							.send();
					Thread.sleep(5000L);
				} catch (InterruptedException ix) {
				}
				System.exit(EXIT_RESTART);
			}
		}).start();
	}

	public void shutdown() {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.create().extractCallInfo().info().level(3).msg("Shutting down BSS controller in 5 sec...")
							.send();
					Thread.sleep(5000L);
				} catch (InterruptedException ix) {
				}
				System.exit(EXIT_SHUTDOWN);
			}
		}).start();
	}

	public void checkLowerSlideStatus() throws Exception {
		if (commsSimulation)
			return;
		Dichroic d = new Dichroic();
		d.setAddress(InetAddress.getByName(lowerSlideHostName));
		d.setPortNumber(lowerSlidePort);

		lowerSlidePosition = d.getPosition();

	}

	public void checkUpperSlideStatus() throws Exception {
		/*
		 * if (commsSimulation) return; Dichroic d = new Dichroic();
		 * d.setAddress(InetAddress.getByName(upperSlideHostName));
		 * d.setPortNumber(upperSlidePort);
		 * 
		 * upperSlidePosition = d.getPosition();
		 */

	}

	public Map<String, Object> getStatus() {

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("comms", (commsSimulation ? "comms.simulation" : "arduino"));

		// slide-1 (upper)
		if (commsSimulation)
			map.put("upper.slide.comms.status", "100.0% simulated");
		else
			map.put("upper.slide.comms.status", "" + (double) 100 * checker.getCountUpperSlideStatus() / 10.0 + "%");
		map.put("upper.slide.status", upperSlideStatus);
		map.put("upper.slide.status.label", statusToString(upperSlideStatus));
		map.put("upper.slide.name", upperSlide.getSlideName());
		map.put("upper.slide.position", upperSlidePosition);
		map.put("upper.slide.host", upperSlideHostName);
		map.put("upper.slide.port", upperSlidePort);
		map.put("upper.slide.filter.name", getSlideFilterName(upperSlide.getSlide(), upperSlide.getElementName()));
		map.put("upper.slide.filter.class.name", upperSlide.getElementName());

		// slide-2 (lower)
		if (commsSimulation)
			map.put("lower.slide.comms.status", "100.0% simulated");
		else
			map.put("lower.slide.comms.status", "" + (double) 100 * checker.getCountLowerSlideStatus() / 10.0 + "%");
		map.put("lower.slide.status", lowerSlideStatus);
		map.put("lower.slide.status.label", statusToString(lowerSlideStatus));
		map.put("lower.slide.name", lowerSlide.getSlideName());
		map.put("lower.slide.position", lowerSlidePosition);
		map.put("lower.slide.host", lowerSlideHostName);
		map.put("lower.slide.port", lowerSlidePort);
		map.put("lower.slide.filter.name", getSlideFilterName(lowerSlide.getSlide(), lowerSlide.getElementName()));
		map.put("lower.slide.filter.class.name",  lowerSlide.getElementName());

		// tiptilt
		// map.put("tip.tilt.offset", tiptilt.getttttt)
		map.put("lower.tiptilt.x.offset", tiptiltXOffset);
		map.put("lower.tiptilt.y.offset", tiptiltYOffset);
	
		// last ra,dec used
		map.put("lower.tiptilt.target.ra", AstroFormatter.formatHMS(tiptiltRa, ":"));
		map.put("lower.tiptilt.target.dec", AstroFormatter.formatDMS(tiptiltDec, ":"));
		map.put("lower.tiptilt.ra.offset", Math.toDegrees(tiptiltRaOffset) * 3600.0);
		map.put("lower.tiptilt.dec.offset", Math.toDegrees(tiptiltDecOffset) * 3600.0);
		// last mode (xy or radec)
		map.put("lower.tiptilt.mode", tiptiltMode);
		// last instrument
		map.put("lower.tiptilt.instrument", (tiptiltInstrumentName != null ? tiptiltInstrumentName : "Unknown"));
		// last rotator
		map.put("lower.tiptilt.rotskypa", Math.toDegrees(tiptiltRotSkyPa));

		return map;
	}

	public boolean isCommsSimulation() {
		return commsSimulation;
	}
	
	public boolean isFullSimulation() {
		return fullSimulation;
	}

	private String getSlideFilterName(int slide, String elementName) {
		String key = null;
		if (slide == XOpticalSlideConfig.SLIDE_UPPER)
			key = "upper";
		else if (slide == XOpticalSlideConfig.SLIDE_LOWER)
			key = "lower";

		String actualKey = key + ".slide." + elementName + ".filter.name";
		logger.create().extractCallInfo().info().level(3).msg("Get key: " + actualKey).send();
		return (String) slideFilterNameMap.get(actualKey);

	}

	public boolean hasUpperElement(String elementName) {
		String key = "upper.slide." + elementName + ".filter.position";
		return slideFilterNameMap.containsKey(key);
	}
	
	public boolean hasLowerElement(String elementName) {
		String key = "lower.slide." + elementName + ".filter.position";
		return slideFilterNameMap.containsKey(key);
	}
	
	private int getSlideFilterPosition(int slide, String elementName) {
		String key = null;
		if (slide == XOpticalSlideConfig.SLIDE_UPPER)
			key = "upper";
		else if (slide == XOpticalSlideConfig.SLIDE_LOWER)
			key = "lower";

		String actualKey = key + ".slide." + elementName + ".filter.position";
		logger.create().extractCallInfo().info().level(3).msg("Get key: " + actualKey).send();

		String strposn = (String) slideFilterNameMap.get(actualKey);
		int pos = Integer.parseInt(strposn);
		// TODO ERROR EXCEPTION IF NOT INT
		return pos;
	}

	/**
	 * @param slideConfig
	 * @throws Exception
	 */
	public void moveUpperSlide(XOpticalSlideConfig slideConfig) throws Exception {
		logger.create().extractCallInfo().info().level(3)
				.msg("Requested to move upper slide to: " + slideConfig.getElementName()).send();

		logger.create().extractCallInfo().info().level(3)
				.msg("Setting upper slide to: " + slideConfig.getElementName()).send();

		int posn = getSlideFilterPosition(XOpticalSlideConfig.SLIDE_UPPER, slideConfig.getElementName());
		upperSlidePosition = posn;
		upperSlide.setElementName(slideConfig.getElementName());
		logger.create().extractCallInfo().info().level(3)
				.msg("Set upper slide to: " + upperSlidePosition + ", " + upperSlide.getElementName()).send();
	}

	/**
	 * @param slideConfig
	 * @throws Exception
	 */
	public void moveLowerSlide(XOpticalSlideConfig slideConfig) throws Exception {

		logger.create().extractCallInfo().info().level(3)
				.msg("Requested to move lower slide to: " + slideConfig.getElementName()).send();

		int slidePosition = getSlideFilterPosition(XOpticalSlideConfig.SLIDE_LOWER, slideConfig.getElementName());

		if (!commsSimulation) {

			logger.create().extractCallInfo().info().level(3).msg("Creating Arduino connection...").send();
			Dichroic d = new Dichroic();
			d.setAddress(InetAddress.getByName(lowerSlideHostName));
			d.setPortNumber(lowerSlidePort);

			int slideActual = d.getPosition();
			// we could decide not to move at this point if its already in
			// position
			// statusSuccessCount = statusSuccessCount+1 % 10;

			logger.create().extractCallInfo().info().level(3).msg("Moving the lower slide...").send();

			lowerSlideStatus = XOpticalSlideConfig.SLIDE_STATUS_MOVING;
			try {
				d.move(slidePosition);
				lowerSlideStatus = XOpticalSlideConfig.SLIDE_STATUS_IN_POSITION;
			} catch (Exception e) {
				lowerSlideStatus = XOpticalSlideConfig.SLIDE_STATUS_ERROR;
			}

			logger.create().extractCallInfo().info().level(3)
					.msg("Finished moving lower slide to: " + slidePosition + ", " + slideConfig.getElementName())
					.send();
		} else {
			logger.create().extractCallInfo().info().level(3).msg("Simulating lower slide move").send();
			lowerSlideStatus = XOpticalSlideConfig.SLIDE_STATUS_MOVING;

			try {
				Thread.sleep(5000L);
			} catch (InterruptedException ix) {
			}

			lowerSlideStatus = XOpticalSlideConfig.SLIDE_STATUS_IN_POSITION;

			logger.create()
					.extractCallInfo()
					.info()
					.level(3)
					.msg("Finished simulated moving lower slide to: " + slidePosition + ", "
							+ slideConfig.getElementName()).send();

		}
		lowerSlidePosition = slidePosition;
		lowerSlide.setElementName(slideConfig.getElementName());
		logger.create().extractCallInfo().info().level(3)
				.msg("Set lower slide to: " + lowerSlidePosition + ", " + lowerSlide.getElementName()).send();
	}

	/**
	 * Perform an absolute offset of the tiptilt.
	 * 
	 * @param raoffset
	 *            RA offset (rads).
	 * @param decoffset
	 *            Dec offset (rads).
	 * @param instrumentName
	 *            Name of the instrument to be used.
	 * @param rotatorCorrection
	 *            Correction to rotator.
	 * @param ra
	 *            Target RA (rads).
	 * @param dec
	 *            Target Dec (rads).
	 */
	public void offsetLowerTiptiltRaDec(double raoffset, double decoffset, String instrumentName,
			double rotatorCorrection, double ra, double dec) {

		// TODO send stuff to lower tiptilt
		// Tiptilt tiptilt = new TipTilt();
		// tiptilt.setOffset(x,y);

		// TODO record this info for any status and fits requests.
		// last micron offsets sent to ttmech
		tiptiltXOffset = 0.0;
		tiptiltYOffset = 0.0;
		// last ra,dec used
		tiptiltRa = ra;
		tiptiltDec = dec;
		tiptiltRaOffset = raoffset;
		tiptiltDecOffset = decoffset;
		// last mode (xy or radec)
		tiptiltMode = 1;
		// last instrument
		tiptiltInstrumentName = instrumentName;
		// last rotator
		tiptiltRotSkyPa = rotatorCorrection;

	}

	public void offsetLowerTiptiltXY(double xOffset, double yOffset, String instrumentName) throws Exception {

		// Set all stored variables TODO (the radec ones may want setting -99 or
		// likes.).

		tiptiltXOffset = xOffset;
		tiptiltYOffset = yOffset;

		// last ra,dec used
		tiptiltRa = 0.0;
		tiptiltDec = 0.0;
		tiptiltRaOffset = 0.0;
		tiptiltDecOffset = 0.0;
		// last mode (xy or radec)
		tiptiltMode = 2;
		// last instrument
		tiptiltInstrumentName = instrumentName;
		// last rotator
		tiptiltRotSkyPa = 0.0;
	}

	/**
	 * @return the upperSlide
	 */
	public XOpticalSlideConfig getUpperSlide() {
		return upperSlide;
	}

	/**
	 * @return the lowerSlide
	 */
	public XOpticalSlideConfig getLowerSlide() {
		return lowerSlide;
	}

	public double getFocusOffset(String instrumentName) throws Exception {

		if (instrumentName == null)
			throw new Exception("No instrument for getfocus");

		String upperSlideElementName = upperSlide.getElementName();
		String lowerSlideElementName = lowerSlide.getElementName();

		if (instrumentName.equals("IO:I")) {
			try {
				return focusOffsetMap.getDoubleValue("upper.slide." + upperSlideElementName + ".reflect.dfocus");
			} catch (Exception e) {
				throw new Exception("IO:I cannot be used with upper slide set to: " + upperSlide.getElementName());
			}

		} else if (instrumentName.equals("IO:O")) {

			try {
				return focusOffsetMap.getDoubleValue("upper.slide." + upperSlideElementName + ".transmit.dfocus")
						+ focusOffsetMap.getDoubleValue("lower.slide." + lowerSlideElementName + ".reflect.dfocus");
			} catch (Exception e) {
				throw new Exception("IO:O cannot be used with: " + upperSlideElementName + "+" + lowerSlideElementName);
			}

		} else if (instrumentName.equals("IO:THOR") || instrumentName.equals("THOR")) {

			try {
				return focusOffsetMap.getDoubleValue("upper.slide." + upperSlideElementName + ".transmit.dfocus")
						+ focusOffsetMap.getDoubleValue("lower.slide." + lowerSlideElementName + ".transmit.dfocus");
			} catch (Exception e) {
				throw new Exception("IO:THOR cannot be used with: " + upperSlideElementName + "+" + lowerSlideElementName);
			}

			
		} else
			throw new Exception("Unknown instrument: " + instrumentName);

	}

	public Vector getFits(String instrumentName) throws Exception {

		if (instrumentName == null)
			throw new Exception("No instrument for getfits");

		String upperSlideElementName = upperSlide.getElementName();
		String lowerSlideElementName = lowerSlide.getElementName();
		
		Vector v = new Vector();

		double bssfocus = 0.0;
		FitsHeaderCardImage fBssFocus = null;
		try {
			bssfocus = getFocusOffset(instrumentName);
			fBssFocus = new FitsHeaderCardImage("BSSDFOC", new Double(bssfocus), "BSS Focus offset", "mm", 1);
		} catch (Exception e) {
			fBssFocus = new FitsHeaderCardImage("BSSDFOC", new Double(0.0), "BSS Focus offset is INVALID", "mm", 1);
		}
		v.add(fBssFocus);

		if (instrumentName.equals("IO:O")) {
			
			String filt6class = upperSlide.getElementName();
			String filt6name = getSlideFilterName(upperSlide.getSlide(), filt6class);
		
			FitsHeaderCardImage fFilt6name = new FitsHeaderCardImage("FILTER6", filt6class, "Upper slide element type", "", 2);
			v.add(fFilt6name);
			
			FitsHeaderCardImage fFilt6Id = new FitsHeaderCardImage("FILTERI6", filt6name, "Upper slide element id", "", 3);
			v.add(fFilt6Id);
			
			
			String filt5class = config.getProperty("lower.tiptilt.filter.class.name", "AlMirror");
			String filt5name = config.getProperty("lower.tiptilt.filter.name", "CVI-ProtAl-SciFold-02");
			
			FitsHeaderCardImage fFilt5name = new FitsHeaderCardImage("FILTER5", filt5class, "Lower tiptilt element type", "", 4);
			v.add(fFilt5name);
			
			FitsHeaderCardImage fFilt5Id = new FitsHeaderCardImage("FILTERI5", filt5name, "Lower tiptilt element id", "", 5);
			v.add(fFilt5Id);
			
			
			String filt4class = lowerSlide.getElementName();
			String filt4name = getSlideFilterName(lowerSlide.getSlide(), filt4class);
					
			FitsHeaderCardImage fFilt4name = new FitsHeaderCardImage("FILTER4", filt4class, "Lower slide element type", "", 6);
			v.add(fFilt4name);
			
			FitsHeaderCardImage fFilt4Id = new FitsHeaderCardImage("FILTERI4", filt4name, "Lower slide element id", "", 7);			
			v.add(fFilt4Id);
			
		} else if (instrumentName.equals("IO:THOR")) {

			// TODO THOR will be different again !!!
			
			String filt5class = config.getProperty("lower.tiptilt.filter.class.name", "AlMirror");
			String filt5name = config.getProperty("lower.tiptilt.filter.name", "CVI-ProtAl-SciFold-02");
			
			FitsHeaderCardImage fFilt5name = new FitsHeaderCardImage("FILTER3", filt5class, "Lower tiptilt element type", "", 4);
			v.add(fFilt5name);
			
			FitsHeaderCardImage fFilt5Id = new FitsHeaderCardImage("FILTERI3", filt5name, "Lower tiptilt element id", "", 5);
			v.add(fFilt5Id);
			
			
			String filt4class = lowerSlide.getElementName();
			String filt4name = getSlideFilterName(lowerSlide.getSlide(), filt4class);
					
			FitsHeaderCardImage fFilt4name = new FitsHeaderCardImage("FILTER2", filt4class, "Lower slide element type", "", 6);
			v.add(fFilt4name);
			
			FitsHeaderCardImage fFilt4Id = new FitsHeaderCardImage("FILTERI2", filt4name, "Lower slide element id", "", 7);			
			v.add(fFilt4Id);
		} else if (instrumentName.equals("IO:I")) {
			
			// TODO these are all rubbish now 
			
			String filt4class = upperSlide.getElementName();
			String filt4name = getSlideFilterName(upperSlide.getSlide(), filt4class);
			
			FitsHeaderCardImage fFilt4name = new FitsHeaderCardImage("FILTER3", filt4class, "Filter 4 type", "", 2);
			v.add(fFilt4name);
		
			FitsHeaderCardImage fFilt4Id = new FitsHeaderCardImage("FILTERI3", filt4name, "Filter 4 type", "", 3);
			v.add(fFilt4Id);
			
			FitsHeaderCardImage fFilt3name = new FitsHeaderCardImage("FILTER3", "Clear", "Filter 3 type", "", 4);
			v.add(fFilt3name);
			
			FitsHeaderCardImage fFilt3Id = new FitsHeaderCardImage("FILTERI3", "Clear", "Filter 3", "", 5);
			v.add(fFilt3Id);
			

		} else
			throw new Exception("Illegal instrument for getfits: " + instrumentName);

		return v;
	}

	public String statusToString(int status) {
		switch (status) {
		case XOpticalSlideConfig.SLIDE_STATUS_IN_POSITION:
			return "IN_POSN";
		case XOpticalSlideConfig.SLIDE_STATUS_MOVING:
			return "MOVING";
		case XOpticalSlideConfig.SLIDE_STATUS_ERROR:
			return "ERROR";
		default:
			return "UNKNOWN";
		}
	}

	// TODO a load of instrument config stuff

}
