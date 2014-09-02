/**
 * 
 */
package ngat.bss;

/**
 * @author eng
 *
 */
public class BeamStatusCheckThread extends Thread {

	private BeamSteeringController bss;
	
	private int countLowerSlideStatus;
	
	private int countUpperSlideStatus;
	
	/**
	 * @param bss
	 */
	public BeamStatusCheckThread(BeamSteeringController bss) {
		super();
		this.bss = bss;
	}



	public void run() {
	
		while (true) {
		
			try {
				bss.checkLowerSlideStatus();
				countLowerSlideStatus++;
				if (countLowerSlideStatus > 10)
					countLowerSlideStatus = 10;
			} catch (Exception e) {
				e.printStackTrace();
				countLowerSlideStatus--;
				if (countLowerSlideStatus < 0)
					countLowerSlideStatus = 0;
			}
			
			try {
				bss.checkUpperSlideStatus();
				countUpperSlideStatus++;
				if (countUpperSlideStatus > 10)
					countUpperSlideStatus = 10;
			} catch (Exception e) {
				e.printStackTrace();
				countUpperSlideStatus--;
				if (countUpperSlideStatus < 0)
					countUpperSlideStatus = 0;
			}
			
			
			try {Thread.sleep(60000L);}catch (InterruptedException ix) {}
		}
		
	}



	/**
	 * @return the countLowerSlideStatus
	 */
	public int getCountLowerSlideStatus() {
		return countLowerSlideStatus;
	}



	/**
	 * @return the countUpperSlideStatus
	 */
	public int getCountUpperSlideStatus() {
		return countUpperSlideStatus;
	}
	
	
	
}
