package FishFight;

import javax.microedition.lcdui.Image;

import cn.ohyeah.itvgame.model.Authorization;
import cn.ohyeah.stb.game.SGraphics;
import cn.ohyeah.stb.game.ServiceWrapper;
import cn.ohyeah.stb.ui.DrawUtil;


/**
 * 计费订购
 * @author chengaoying
 *
 */
public class Purchase {
	
	private FiCanvas fiCanvas ;
	private Image normalBgImg = null;
	private Image waitBgImg = null;
	private Image errorBgImg = null;
	private Image buttonBackImg = null;
	private Image buttonOKImg = null;
	private Image selectImg = null;
	public int menuPos = 0;
	private int result = -1;
	private String msg = null;
	
	public Purchase(FiCanvas ficanvas){
		this.fiCanvas = ficanvas;
	}
	
	public void loadRes() {
		if (normalBgImg == null) {
			try {
				normalBgImg = Image.createImage("/res/bg_info.jpg");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (waitBgImg == null) {
			try {
				waitBgImg = Image.createImage("/res/bg_wait.jpg");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (errorBgImg == null) {
			try {
				errorBgImg = Image.createImage("/res/bg_error.jpg");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (buttonBackImg == null) {
			try {
				buttonBackImg = Image.createImage("/res/btn_back.png");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (buttonOKImg == null) {
			try {
				buttonOKImg = Image.createImage("/res/btn_ok.png");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (selectImg == null) {
			try {
				selectImg = Image.createImage("/res/select.png");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void releaseRes() {
		normalBgImg = null;
		waitBgImg = null;
		errorBgImg = null;
		buttonBackImg = null;
		buttonOKImg = null;
		selectImg = null;
	}
	/**
	 * 判断用户是否是充值用户
	 * @return
	 */
	public boolean isPaidToPlay() {
		boolean isPaidToPlay = false;
		Authorization authorization;
		ServiceWrapper sw = fiCanvas.getServiceWrapper();
		try {
			//ownProps = propService.queryOwnPropList(fiCanvas.accountId,	fiCanvas.accountId);
			authorization = sw.queryAuthorization();
			if (authorization != null){
				System.out.println("authorizationType:" + authorization.getAuthorizationType());
				System.out.println("AuthorizationStartTime:" + authorization.getAuthorizationStartTime());
				System.out.println("AuthorizationEndTime:" + authorization.getAuthorizationEndTime());
				if(authorization.getAuthorizationType() == Authorization.AUTHORIZATION_PERIOD){
					isPaidToPlay = true;
				}
			}
			else {
				System.out.println("鉴权信息为空");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isPaidToPlay;
	}
    /**
     * 订购
     * @return
     */
	public int doPurchase() {
		ServiceWrapper sw = fiCanvas.getServiceWrapper();
		try {
			sw.subscribeProduct("period", "大鱼吃小鱼订购");
			result = sw.getServiceResult();
			System.out.println("result: " + result);
			msg = sw.getServiceMessage();
			if(msg == null){
				msg = "连接失败,请稍后再试!";
			}
			System.out.println("错误信息: " + msg);
		} catch (Exception e) {
			e.printStackTrace();
			msg = "连接失败,原因"+e.getMessage();
			result = -1;
		}
		return result;
	}

	public void draw(SGraphics g) { //订购提示页面
		int btnOkOffsetX = 166, btnOKOffsetY = 390;
		int btnBackOffsetX = 362, btnBackOffsetY = 390;
		loadRes();
		g.drawImage(normalBgImg, 0, 0, Tools.GRAPHCS_TOP_LEFT);
		g.setColor(255, 255, 255);
		int offsetX = 74;
		g.drawString("您将要订购的游戏为：" + FiCanvas.gameNameStr, offsetX, 131,
				Tools.GRAPHCS_TOP_LEFT);
		g.drawString("资费：" + FiCanvas.gamePriceStr, offsetX, 205,
				Tools.GRAPHCS_TOP_LEFT);
		g.drawString("说明：" + FiCanvas.gamePriceDescStr, offsetX, 275,
				Tools.GRAPHCS_TOP_LEFT);
		g.drawImage(buttonOKImg, btnOkOffsetX, btnOKOffsetY,
				Tools.GRAPHCS_TOP_LEFT);
		g.drawImage(buttonBackImg, btnBackOffsetX, btnBackOffsetY,
				Tools.GRAPHCS_TOP_LEFT);
		g.setColor(255, 0, 0);
		if (menuPos == 0) {
			//g.drawImage(selectImg, btnOkOffsetX - 1, btnOKOffsetY,
			//		Tools.GRAPHCS_TOP_LEFT);
			DrawUtil.drawRect(g, btnOkOffsetX - 1,  btnOKOffsetY, buttonOKImg.getWidth(), buttonOKImg.getHeight(), 3, 0XFF00FF);
		} else if (menuPos == 1) {
			//g.drawImage(selectImg, btnBackOffsetX - 1, btnBackOffsetY,
			//		Tools.GRAPHCS_TOP_LEFT);
			DrawUtil.drawRect(g, btnBackOffsetX - 1,  btnBackOffsetY, buttonBackImg.getWidth(), buttonBackImg.getHeight(), 3, 0XFF00FF);
		}

	}
	
	public void draw2(SGraphics g) { //订购成功
		int btnBackOffsetX = 362, btnBackOffsetY = 390;
		loadRes();
		g.drawImage(normalBgImg, 0, 0, Tools.GRAPHCS_TOP_LEFT);
		g.setColor(255, 255, 255);
		int offsetX = 74;
		g.drawString(Tools.COMMON_SUCCESS_MSG, offsetX, 131,
				Tools.GRAPHCS_TOP_LEFT);
		
		g.drawImage(buttonBackImg, btnBackOffsetX, btnBackOffsetY,
				Tools.GRAPHCS_TOP_LEFT);
		g.setColor(255, 0, 0);
		
		/*g.drawImage(selectImg, btnBackOffsetX - 1, btnBackOffsetY,
					Tools.GRAPHCS_TOP_LEFT);*/
		DrawUtil.drawRect(g, btnBackOffsetX - 1,  btnBackOffsetY, buttonBackImg.getWidth(), buttonBackImg.getHeight(), 3, 0XFF00FF);

	}
	
	public void draw3(SGraphics g) { //订购失败
		int btnBackOffsetX = 362, btnBackOffsetY = 390;
		loadRes();
		g.drawImage(normalBgImg, 0, 0, Tools.GRAPHCS_TOP_LEFT);
		g.setColor(255, 255, 255);
		int offsetX = 74;
		g.drawString("错误信息:  " + msg, offsetX, 131,
				Tools.GRAPHCS_TOP_LEFT);
		
		g.drawImage(buttonBackImg, btnBackOffsetX, btnBackOffsetY,
				Tools.GRAPHCS_TOP_LEFT);
		g.setColor(255, 0, 0);
		
		/*g.drawImage(selectImg, btnBackOffsetX - 1, btnBackOffsetY,
					Tools.GRAPHCS_TOP_LEFT);*/
		DrawUtil.drawRect(g, btnBackOffsetX - 1,  btnBackOffsetY, buttonBackImg.getWidth(), buttonBackImg.getHeight(), 3, 0XFF00FF);
	}

}


