package fishFish;
import javax.microedition.lcdui.Image;

import cn.ohyeah.itvgame.model.Authorization;
import cn.ohyeah.itvgame.model.OwnProp;
import cn.ohyeah.itvgame.service.AccountService;
import cn.ohyeah.itvgame.service.PropService;
import cn.ohyeah.itvgame.service.SubscribeService;
import cn.ohyeah.stb.game.SGraphics;
import cn.ohyeah.stb.ui.DrawUtil;


/**
 * �ƷѶ���
 * @author chengaoying
 *
 */
public class Purchase {
	
	private FiCanvas fiCanvas = null;
	private SubscribeService subscribeService = null;
	private Image normalBgImg = null;
	private Image waitBgImg = null;
	private Image errorBgImg = null;
	private Image buttonBackImg = null;
	private Image buttonOKImg = null;
	private Image selectImg = null;
	public int i = 1;
	public int menuPos = 0;
	private int result = -1;
	private String msg = null;
	private AccountService accountService = null;
	
	

	public Purchase(FiCanvas fiCanvas) {
		    this.fiCanvas = fiCanvas;
		    subscribeService = new SubscribeService(fiCanvas.protocolLocation); 
		    accountService = new AccountService(fiCanvas.protocolLocation);
		    //System.out.println("protocolLocation:" + fiCanvas.protocolLocation);
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
	 * �ж��û��Ƿ��ǳ�ֵ�û�
	 * @return
	 */
	public boolean isPaidToPlay() {
		boolean isPaidToPlay = false;
		Authorization authorization;
		try {
			//ownProps = propService.queryOwnPropList(fiCanvas.accountId,	fiCanvas.accountId);
			authorization = accountService.getAuthorization(fiCanvas.accountId, fiCanvas.productId);
			if (authorization != null){
				System.out.println("authorizationType:" + authorization.getAuthorizationType());
				System.out.println("AuthorizationStartTime:" + authorization.getAuthorizationStartTime());
				System.out.println("AuthorizationEndTime:" + authorization.getAuthorizationEndTime());
				if(authorization.getAuthorizationType() == Authorization.AUTHORIZATION_PERIOD){
					isPaidToPlay = true;
				}
			}
			else {
				System.out.println("��Ȩ��ϢΪ��");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isPaidToPlay;
	}
    /**
     * ����
     * @return
     */
	public int doPurchase() {
		try {
			 subscribeService.subscribeProduct("", fiCanvas.accountId, 
					fiCanvas.accountName, fiCanvas.userToken, fiCanvas.productId, "period", "�����С�㶩��", "");
			result = subscribeService.getResult();
			System.out.println("result: " + result);
			msg = subscribeService.getMessage();
			if(msg == null){
				msg = "����ʧ��,���Ժ�����!";
			}
			System.out.println("������Ϣ: " + msg);
		} catch (Exception e) {
			e.printStackTrace();
			msg = "����ʧ��,ԭ��"+e.getMessage();
			result = -1;
		}
		return result;
	}

	public void draw(SGraphics g) { //������ʾҳ��
		int btnOkOffsetX = 166, btnOKOffsetY = 390;
		int btnBackOffsetX = 362, btnBackOffsetY = 390;
		loadRes();
		g.drawImage(normalBgImg, 0, 0, Tools.GRAPHCS_TOP_LEFT);
		g.setColor(255, 255, 255);
		int offsetX = 74;
		g.drawString("����Ҫ��������ϷΪ��" + fiCanvas.gameNameStr, offsetX, 131,
				Tools.GRAPHCS_TOP_LEFT);
		g.drawString("�ʷѣ�" + fiCanvas.gamePriceStr, offsetX, 205,
				Tools.GRAPHCS_TOP_LEFT);
		g.drawString("˵����" + fiCanvas.gamePriceDescStr, offsetX, 275,
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
	
	public void draw2(SGraphics g) { //�����ɹ�
		int btnOkOffsetX = 166, btnOKOffsetY = 390;
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
	
	public void draw3(SGraphics g) { //����ʧ��
		int btnOkOffsetX = 166, btnOKOffsetY = 390;
		int btnBackOffsetX = 362, btnBackOffsetY = 390;
		loadRes();
		g.drawImage(normalBgImg, 0, 0, Tools.GRAPHCS_TOP_LEFT);
		g.setColor(255, 255, 255);
		int offsetX = 74;
		g.drawString("������Ϣ:  " + msg, offsetX, 131,
				Tools.GRAPHCS_TOP_LEFT);
		
		g.drawImage(buttonBackImg, btnBackOffsetX, btnBackOffsetY,
				Tools.GRAPHCS_TOP_LEFT);
		g.setColor(255, 0, 0);
		
		/*g.drawImage(selectImg, btnBackOffsetX - 1, btnBackOffsetY,
					Tools.GRAPHCS_TOP_LEFT);*/
		DrawUtil.drawRect(g, btnBackOffsetX - 1,  btnBackOffsetY, buttonBackImg.getWidth(), buttonBackImg.getHeight(), 3, 0XFF00FF);
	}

}

