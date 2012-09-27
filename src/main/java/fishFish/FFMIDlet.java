package fishFish;


import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class FFMIDlet extends MIDlet {
  static FFMIDlet instance;
  static FiCanvas displayable;
  static Thread t;
  public FFMIDlet() {
    instance = this;
  }

  public void startApp() {
    try {
        if (displayable == null) {
            displayable = new FiCanvas(this);
            Display.getDisplay(this).setCurrent(displayable);
            t = new Thread(displayable);
            t.start();
        }
    } catch (Exception e) {
        destroyApp(false);
        notifyDestroyed();
    }
  }

  public void pauseApp() {
  }

  public void destroyApp(boolean unconditional) {
  }

  public static void quitApp() {
	//displayable.isPaidToPlay = false;  
   // instance.destroyApp(true);
    instance.notifyDestroyed();
   // instance = null;
  }

}


class Role {
	  boolean isOwn;
	  int mapx;
	  int mapy;
	  int frame;
	  int direction;
	  int experience;
	  int scores; //排行积分
	//  int harm;
	  int money;
	  int velocityTime;//npc行动时间
	  int velocityNum;//主角为行动值   npc为行动索引，对应FiCanvas.speed
	  int id;
	//  int bulk;
	  int status,status2;
	  int nonceLife; //生命
	  int lifeNum;
	  Role cahoot;
}
