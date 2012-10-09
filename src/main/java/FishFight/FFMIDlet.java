package FishFight;


import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


public class FFMIDlet extends MIDlet {
private static FFMIDlet instance;
	
	public FFMIDlet(){
		instance = this;
	}
	
	public static FFMIDlet getInstance() {
		return instance;
	}

	protected void destroyApp(boolean unconditional)throws MIDletStateChangeException {}

	protected void pauseApp() {}

	protected void startApp() throws MIDletStateChangeException {
		Display.getDisplay(this).setCurrent(FiCanvas.instance);
		System.out.println(111111111);
		new Thread(FiCanvas.instance).start();
	}
}
