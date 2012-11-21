package FishFight;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

//import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.midlet.MIDlet;

import cn.ohyeah.itvgame.model.GameAttainment;
import cn.ohyeah.itvgame.model.GameRanking;
import cn.ohyeah.itvgame.model.GameRecord;
import cn.ohyeah.stb.game.GameCanvasEngine;
import cn.ohyeah.stb.game.SGraphics;
import cn.ohyeah.stb.game.ServiceWrapper;
import cn.ohyeah.stb.key.KeyCode;
import cn.ohyeah.stb.util.DateUtil;

public class FiCanvas extends GameCanvasEngine implements Runnable {
	public static String gameNameStr = "大鱼吃小鱼";
	public static String gamePriceStr = "3元"; //"3元";
	public static String gamePriceDescStr = "按次收费，有效期30天，无需退订";
	//public static int TRY_PLAY_TIME = 60000; // try run time (second)
	public static boolean isPaidToPlay = false; // is paid
	
	private final byte TopLeft = Graphics.TOP | Graphics.LEFT;
	private final byte BottomLeft = Graphics.BOTTOM | Graphics.LEFT;
	private final byte BottomRight = Graphics.BOTTOM | Graphics.RIGHT;
	
	public static FiCanvas instance = buildGameEngine();
	
	private static FiCanvas buildGameEngine() {
		return new FiCanvas(FFMIDlet.getInstance());
	}
	
	Purchase purchase;
	private SGraphics g;
	private final int ScreenW, ScreenH;
	private int FontW = 12, FontH = 15;
	private Font largeFont, normalFont, curFont;
    //主角
	String[] hero = { "/own_fish1.png", "/own_fish2.png", "/own_fish3.png",
			"/own_fish4.png" };
	String[] heroEye = {"/own_fish1_eye.png","/own_fish2_eye.png","/own_fish3_eye.png","/own_fish4_eye.png"};
	String[] heroWing = {"/own_fish1_wing.png","/own_fish2_wing.png","/own_fish3_wing.png","/own_fish4_wing.png"};
	String[] wudi = {"/wudi1.png","/wudi2.png","/wudi3.png","/wudi4.png"};
	private int[][] wudicoordinate = {{4,2},{4,7},{2,7},{3,7}
	                                  ,{4,2},{4,7},{15,7},{25,5}};
	private Image imgOwnwing, imgOwneye;
	private int[][] coordinate = {{8, 18},{10, 19},{14, 41},{13, 54}, //眼睛相对坐标
			                      {14, 18}, {20, 19}, {23, 41},{25, 54}  //眼睛逆向时的相对坐标
	                                }; 
	private int[][] coordinate2 = {{19, 30},{25, 29},{23, 62},{23, 88}, //翅膀相对坐标
			                        {27, 30},{36, 29}, {44, 62},{60, 88} //翅膀逆向时的相对坐标
	                                }; 
	//其他鱼的眼睛和翅膀
	private Image[] eyes = new Image[13];
	private Image[] wings = new Image[13];
	//乌龟坐标
	private int[][] footCoordinate = {{23, 24},
			                          {70, 24}
	                                  };
	private int[][] foot2Coordinate = {{85, 18}, 
			                           {117, 18}
	                                   };
	private int[][] eyeCoordinate = {{6, 21},
	                                 {12, 21}
	                                 };

	private final int[][] npc = {  //npc翅膀和眼睛的宽和高 ,和相应的坐标
	//0-eye-width 1-eye-height 2-wing-width 3-wing-height 4-eye-x 5-eye-y 6-wing-x 7-wing-y
	// 8-eye-x2	9-eye-y2 10-wing-x2	11-wing-y2 (8-11反方向时的坐标)		
			{5, 5, 0, 0, 7, 5, 0, 0, 13, 5, 0, 0 }, // 小鱼苗 -----12
			{6, 6, 27, 19, 8, 19, 21, 20, 18, 19, 50, 20}, // 大肚鱼 -----13
			{7, 7, 15, 20, 18, 14, 35, 29, 25, 14, 45, 29}, // 海豚  -----14
			{}, //鲨鱼       -----15   
			{5, 5, 15, 14, 7, 11, 17, 13, 12, 11, 30, 13}, // 鲤鱼      -----16 
			{8, 8, 18, 9, 6, 20, 24, 36, 13, 20, 42, 36}, //  蓝色热带鱼    -----17 
			{8, 8, 23, 20, 12, 28, 28, 37, 18, 28, 48, 37}, //扁鱼       -----18 
			{5, 5, 20, 27, 13, 27, 36, 39, 19, 27, 55, 39}, //秋刀鱼       -----19 
			{3, 3, 7, 11, 8, 2, 22, 9, 13, 2, 22, 9}, // 长鱼苗  -----20 
			{5, 5, 24, 29, 7, 21, 22, 30, 15, 21, 45, 30}, // 金鱼   -----21 
			{}, //乌龟       -----22 
			{19, 19, 83, 109, 22, 50, 42, 0, 38, 50, 126, 0}, //食人鱼       -----23 
			{7, 7, 12, 11, 6, 6, 20, 8, 12, 6, 26, 8}, //黄鱼       -----24
	};

	private final int[][] Gate = {// 地图尺寸
	/*
	 * {176,208,6}, {176,208,6}, {176,208,6}
	 */
	{ 644, 534, 6 }, { 644, 534, 6 }, { 640, 526, 6 }, };

	private final int Limit[][][] = {// 刷鱼方式
	{ { 24, /*12,*/ 20, 16, 15 }, { 2, 1, 1, 1, 1 } },
			{ { 13, 21, 17, 15 }, { 1, 1, 1, 1 } },
			{ { 14, 22, 18, 15 }, { 1, 1, 1, 1 } },
			{ { 24, 23, 15 }, { 1, 1, 2 } },
			{ { 24, 23, 19, 15 }, { 2, 1, 1, 2 } }, 
			{ { 24, 15 }, { 3, 5 } }, };
	/*private final int Limit[][][] = {// 刷鱼方式
			{ { 24, 12, 20, 16, 15 }, { 2, 1, 1, 1, 1 } },
					{ { 13, 21, 17, 15 }, { 1, 1, 1, 1 } },
					{ { 14, 22, 18, 15 }, { 1, 1, 1, 1 } },
					{ { 24, 23, 15 }, { 1, 1, 2 } },
					{ { 24, 23, 19, 15 }, { 2, 1, 1, 2 } }, 
					{ { 24, 15 }, { 3, 5 } }, };
     */
	private final int[][] Para = {// 图片尺寸 对应id
	// owner ****************
	// 0-width 1-height 2-bulk 3-exprience(升级所需经验) 4-速度 5-生命总量 6-行动总量
	// 7-行动值恢复间隔时间 8-免伤害系数
			{ 60, 48, 2, 50, 12, 100, 100, 1000, 1 },// 鱼种类0----------0
			{ 81, 61, 4, 350 , 12, 100, 100, 1000, 1 },// 鱼种类1----------1
			{ 110, 107, 6, 800 , 12, 100, 100, 1000, 1 },// 鱼种类2----------2
			{ 130, 139, 8,  1200 , 12, 100, 100, 1000, 1 },// 鱼种类3----------3
			{ 30, 28, 1, 75, 4, 100, 100, 1000, 2 },// 乌龟类0----------4
			{ 38, 36, 2, 350, 4, 100, 100, 1000, 2 },// 乌龟类1----------5
			{ 43, 39, 3, 800, 4, 100, 100, 1000, 2 },// 乌龟类2----------6
			{ 44, 43, 4, 1100, 4, 100, 100, 1000, 2 },// 乌龟类3----------7
			{ 25, 26, 1, 75, 5, 100, 100, 700, 1 },// 企鹅类0----------8
			{ 29, 30, 2, 350, 5, 100, 100, 700, 1 },// 企鹅类1----------9
			{ 33, 35, 3, 800, 5, 100, 100, 700, 1 },// 企鹅类2---------10
			{ 38, 42, 4, 1100, 5, 100, 100, 700, 1 },// 企鹅类3---------11

			// npc *******************
			// 0-width 1-height 2-bulk 3-exprience(提供主角经验) 4-harm 5-money(提供主角钱) (6 7)-控制鱼的眼睛 
			{ 50, 20, 1, 2, 15, 20, 0, 1 },// 小鱼苗---------12
			{ 90, 49, 3, 5, 30, 30, 0, 1 },// 大肚鱼---------13
			{ 142, 33, 5, 10, 45, 35, 0, 1 },// 海豚---------14
			{ 246, 108, 7, 20, 60, 40, 0, 1 },// 鲨鱼---------15
			{ 60, 35, 1, 2, 15, 20, 0, 1 },// 鲤鱼---------16
			{ 85, 42, 3, 5, 30, 30, 0,  1 },// 蓝色热带鱼---------17
			{ 80, 75, 5, 10, 45, 35, 0, 1 },// 扁鱼---------18
			{ 175, 50, 7, 20, 60, 40, 0, 1 },// 秋刀鱼---------19
			{ 73, 13, 1, 2, 15, 20, 0, 1 },// 长鱼苗---------20
			{ 80, 47, 3, 5, 30, 30,0, 1 },// 金鱼---------21
			{ 105, 36, 5, 10, 45, 35, 0, 1 },// 乌龟---------22
			{ 180, 117, 7, 20, 60, 40, 0, 1 },// 食人鱼---------23
			{ 60, 26, 0, 5, 0, 5, 0, 1 },// 黄鱼-----------24
			// prop *******************
			// 0-width 1-height 2(无意义防止数组越界)
			{ 25, 25, 0 },// 闪电------------25
			{ 25, 25, 0 },// 生命药---------26
			{ 25, 25, 0 },// 血药----------27
			{ 25, 25, 0 },// 行动药----------28
			{ 9, 9 },// 泡----------------29
			{ 195, 430, 0 },// 电光-------------30
			{ 25, 25, 0 },// 咔-------------31
			{ 103, 39, 0 },// 骨头-----------32
			{ 58, 58, 0, 20 },// 珍珠-------------33
			{ 64, 59, 0 },// 贝壳-------------34
			{ 32, 32, 0 },// 钱---------------35
			{9, 9}, // 主界面泡  ----------36
			{9, 9}, // 主界面泡  ----------37
	};

	private final int[][] Speed = {// npc移动速度
	{ 3, 6 }, { 6, 3 }, { 9, 0 }, { 6, -3 }, { 3, -6 } };

	private Image  imgMain1, imgMain2, imgFrm, imgSound,
			imgCoin, imgMenu, imgHelp,imgHelp2, imgRank, imgPage ,imgCharge,imgSelect;
	//提示框中的魚
	private Image imgblue,imgyellow,imgbian,imgbigtummy,imgchangyumiao,imgdolphin,
	        imgjinyu,imgliyu,imgqiudao,imgshayu,imgshirenyu,imgturtle,imgxiaoyu;
	
	private Image imgKa, imgPao, imgGu, imgDg, 
			 imgPearl, imgWalk ,imgPao1, imgPao2;
	private Image imgMap, imgInfo, imgLevel, imgLine, imgNumber, 
			imgSelect2, imgPrompt2, imgFish, imgEye;
	private Image imgRole[] = new Image[25];
	private Image imgWudi[] = new Image[4]; 
	//乌龟后腿
	private Image  foot2;

	private long timeStart, timeEnd, timeFps, timeClock, time;
	private long timeFlag;
	private int /*key,*/ state, app, tempx, tempy, tempnum;
	private Random ran = new Random();
	private Role temprole , r, r2;
	private boolean isRed, noHalt;
	private int flag, flag2 = 0 ,flag3 = 0 , flag4 = 0 ; //屏幕上鱼静止时,用于做判断的变量 
	
	// 存档数据
	public static int tollGate2;
	public static int experience;
	public static int scores;
	public static int money;
	public static int nonceLife;
	public static int lifeNum;
	public static int id;
	// 菜单
	private int mainIndex, menuIndex, soundIndex, selectIndex, selectDir, helpIndex;
	private int loopIndex = 0 , countIndex = 0 ,fishIndex = 0 , eyeIndex = 1 ,ownIndex = 1, own2Index = 1, own3Index = 1, own4Index = 1,
	            flagIndex = 1 , flag2Index = 0, owneyeIndex = 0 , ownwingIndex = 0 ,loop2Index = 0 ;
	//控制乌龟
	private int tortoiseIndex = 0, tortoise2Index = 0, teyeIndex = 0, tortoiseflag = 0;
	//控制其他鱼
	private int wingIndex = 0, e2Index = 0 ;
	
	private int p = 1; //主界面泡泡控制变量
	// 关卡
	public static int tollGate = 0;
	private int mapw, maph, screenx, screeny;
	// 角色
	private Vector npcs = new Vector();
	private Vector props = new Vector();
	private Role own;// 主角
	private Role levin;// 闪电(电光)
	private Role coin;
	private int Confine[] = new int[25];// 12到24有用 npc鱼的限制数量
	private int eatNum;
	
	private SaveGameRecord gameRecord = new SaveGameRecord(this);

	// 资源
	private int load = 0, loadVelocity = 0;
	private long clock1;// 记录主角行动值消耗的时间
	private long clock2;// 记录主角行动值恢复的时间
	private int ownBluk = 0;

	//排行
	GameRanking[] rankList = null;
	int rank ;
	int page = 0 ;
	//充值变量
	int rechange = -1 ;

	
	public FiCanvas(MIDlet midlet) {
		super(midlet);
		this.midlet = midlet;
		setRelease(false);
		setFullScreenMode(true);
		g = getSGraphics();
		normalFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN,
				Font.SIZE_MEDIUM);
		if (normalFont.getHeight() > 20) {
			curFont = normalFont;
		} else {
			largeFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN,	Font.SIZE_LARGE);
			curFont = largeFont;
			normalFont = null;
		}
		g.setFont(curFont);
		ScreenW = getWidth();
		ScreenH = getHeight();
		app = 1;
		state = 1;
		time = 100;
		isRed = false;
		noHalt = true;
		purchase = new Purchase(this);
	}

	protected void drawAll(SGraphics g) {
		try {
			// g.setFont(curFont);
			if (state == 1) { // yidong
				drawYidong(g);
			} else if (state == 2) { // bbs
				drawBbs(g);
			} else if (state == 3) {// main
				drawMain(g);
			} else if (state == 4) {// main->sound
				drawSound(g);
			} else if (state == 5) {// about
				// drawAbout(g);
			} else if (state == 6) { // intro
				drawIntro(g);
			} else if (state == 7) {// load
				drawLoad(g);
			} else if (state == 8) {// game
				drawGame(g);
			} else if (state == 9) {// dg
				drawDg(g);
			} else if (state == 10) {// pass
				drawPWO(g);
			} else if (state == 11) {// win
				drawPWO(g);
			} else if (state == 12) {// over
				drawPWO(g);
			} else if (state == 13) {// select
				drawSelect(g);
			} else if (state == 14) {// dead
				drawDead(g);
			} else if (state == 15) {// ready
				drawReady(g);
			} else if (state == 16) {// menu
				drawMenu(g);
			} else if (state == 17) {// menu->sound
				drawSound(g);
			} else if (state == 18) {// main->help
				// drawHelp(g);
				showHelp(g);
			} else if (state == 19) {// menu->help
				//drawHelp(g);
				showHelp(g);
			} else if (state == 20) {
				drawSelect(g);
			} else if (state == 21) {
				drawRanklist(g);   //画排行
			} else if (state == 22) {
				drawPrompt();  
			} else if (state == 23) {
				drawPruchase(g);  //画订购 提示  
			} else if (state == 24) { //订购成功
				drawRechange(g);
			} else if (state == 25) { //订购失败
				drawFail(g);
			} else if (state == 26) { //没有游戏记录时给出的提示
				drawRecord();
			} else if (state == 27) { //金币不足时提示
				drawCoin();
			} else if (state == 28) { //试玩结束弹出提示
				drawCharging();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void loop() {
		try {
			timeClock = System.currentTimeMillis() + 1000;
			while (app == 1) {
				timeStart = System.currentTimeMillis();
				if (noHalt) {
					if (state == 1) { // yidong
						runYidong();
					} else if (state == 2) { // bbs
						runBbs();
					} else if (state == 3) { // main
						runMain();
					} else if (state == 4) { // main->sound
						runSound(3);
					} else if (state == 5) { // about
						runAbout();
					} else if (state == 6) { // intro
						runIntro();
					} else if (state == 7) { // load
						runLoad();
					} else if (state == 8) { // game
						runGame();
					} else if (state == 9) { // dg
						runDg();
					} else if (state == 10) { // pass
						runPWO();
					} else if (state == 11) { // win
						runPWO();
					} else if (state == 12) { // over
						runPWO();
					} else if (state == 13) { // select
						runSelect();
					} else if (state == 14) { // dead
						runDead();
					} else if (state == 15) { // ready
						runReady();
					} else if (state == 16) { // menu
						runMenu();
					} else if (state == 17) { // menu->sound
						runSound(16);
					} else if (state == 18) { // main->help
						runHelp(3);
					} else if (state == 19) { // menu->help
						runHelp(16);
					} else if (state == 20) {
						runSelect(1);
					} else if (state == 21) { //main->Ranklist 排行榜
                        runRanklist();           
					} else if (state == 22) {
						runPrompt();        //升级提示可以吃的鱼的种类
					} else if (state == 23) {  //订购提示
						runPurchase();
					} else if (state == 24) {  //订购成功
						runRechange();
					} else if (state == 25) {  //订购失败
						runFail();
					} else if (state == 26) {  //没有游戏记录时给出提示
						runRecord();
					} else if (state == 27) {  //金币不足时给出提示
						runCoin();
					} else if (state == 28) { //试玩结束弹出提示
						runCharging();
					}
					/*
					 * repaint(); serviceRepaints();
					 */
					drawAll(g);
					flushGraphics();;
					System.gc();
				}
				timeEnd = System.currentTimeMillis();
				timeFps = timeEnd - timeStart;
				try {
					if (timeFps < time) {
						Thread.sleep(time - timeFps);
					} else {
						Thread.sleep(0);
					}
				} catch (Exception e) {
				}
			}
			//FFMIDlet.getInstance().destroyApp(unconditional);
			exit = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void runFail() {  //订购失败
		if (keyState.containsAndRemove(KeyCode.OK)) {// 确定
			purchase.releaseRes();
			state = 3; // main
		}
	}

	private void drawFail(SGraphics g2) {
		purchase.draw3(g2);
	}

	private void runRechange() {  //订购成功
		if (keyState.containsAndRemove(KeyCode.OK)) {// 确定
			purchase.releaseRes();
			state = 20; //订购成功重新开始游戏
		}
	}
	

	private void drawRechange(SGraphics g2) { 
		purchase.draw2(g2);
	}

	private void runPurchase() {  //订购
		
		if (keyState.containsAndRemove(KeyCode.LEFT)) {// 左
			purchase.menuPos = 0;
		} else if (keyState.containsAndRemove(KeyCode.RIGHT)) {// 右
			purchase.menuPos = 1;
		} else if (keyState.containsAndRemove(KeyCode.OK)) {// 确定
			if(purchase.menuPos == 0){
				rechange = purchase.doPurchase();  //rechange 为0 充值成功
				if(rechange == 0) {
					state = 24;
					isPaidToPlay = true;
				} else {
					state = 25;
					isPaidToPlay = false;
				}
			} else if(purchase.menuPos == 1){
				
				isPaidToPlay = false;
				state = 3; // main
			}
		}
	}
	
	private void drawPruchase(SGraphics g) {
		purchase.draw(g);
	}
	
	private int selectFlag = 1;
	private void runCharging() {
		if(keyState.containsAndRemove(KeyCode.LEFT)){
			selectFlag=1;
		}
		if(keyState.containsAndRemove(KeyCode.RIGHT)){
			selectFlag=0;
		}
		if(keyState.containsAndRemove(KeyCode.OK)){
			imgCharge = null;
			imgSelect = null;
			if(selectFlag == 1){
				state = 23; //进入计费界面
			}else if(selectFlag == 0){
				state = 3; //进入主界面
			}
		}
		
	}
	
	private void drawCharging() throws IOException {
		
		for (int i = props.size() - 1; i >= 0; --i) { //清空
			props.removeElementAt(i);
		}
		imgCoin = imgKa = imgPao = imgGu = imgDg = imgLine = null;
		imgPearl = imgWalk = imgMap = imgInfo = imgLevel = imgNumber = null;
		imgPrompt = foot2 = imgProp = imgEye = imgFish = null;
		for (int i = imgRole.length - 1; i >= 0; --i) {
			imgRole[i] = null;
		}
		for (int i = wings.length - 1; i >= 0; --i) {
			wings[i] = null;
		}
		for (int i = eyes.length - 1; i >= 0; --i) {
			eyes[i] = null;
		}
		
		if(imgCharge == null || imgSelect == null){
			imgCharge = Image.createImage("/charge.png");
			imgSelect = Image.createImage("/select.png");
		}
		g.drawImage(imgCharge, 75, 126, TopLeft);
		g.drawRegion(imgSelect, selectFlag*imgSelect.getWidth()/2, 0, imgSelect.getWidth()/2, imgSelect.getHeight()/2, 0, 129, 342, TopLeft);
		g.drawRegion(imgSelect, (selectFlag==0?1:0)*imgSelect.getWidth()/2, imgSelect.getHeight()/2, imgSelect.getWidth()/2, imgSelect.getHeight()/2, 0, 349, 342, TopLeft);

	}

	private void runCoin() {
		if (keyState.containsAndRemove(KeyCode.OK)){
			state = 8; // game
		}
	}
	

	private void drawCoin() {
		int tw = curFont.stringWidth("金币不足300,按确认键返回!") + 30;
		int th = FontH + 20;
		int tx = ScreenW / 2 - tw / 2;
		int ty = ScreenH / 2 - th;
		g.setClip(0, 0, ScreenW, ScreenH);
		g.setColor(26, 131, 238); 
		g.fillRect(tx, ty, tw, th);// 画框
		g.setColor(243, 191, 99); //框的颜色
		g.drawRect(tx - 1, ty - 1, tw + 1, th + 1);// 画边
		g.drawRect(tx - 2, ty - 2, tw + 3, th + 3);// 画边
		g.setColor(0xffffff);
		g.drawString("金币不足300,按确认键返回!", tx + 15, ty + 8, TopLeft);
	}


    //无游戏记录时给的提示
	private void runRecord(){
		if (keyState.containsAndRemove(KeyCode.OK)){
			start = 0;
			state = 3; // main
			mainIndex = 1;
		}
	}
	
	private void drawRecord(){
		int tw = curFont.stringWidth("没有游戏记录,按确认键返回!") + 30;
		int th = FontH + 20;
		int tx = ScreenW / 2 - tw / 2;
		int ty = ScreenH / 2 - th;
		g.setClip(0, 0, ScreenW, ScreenH);
		g.setColor(26, 131, 238); 
		g.fillRect(tx, ty, tw, th);// 画框
		g.setColor(243, 191, 99); //框的颜色
		g.drawRect(tx - 1, ty - 1, tw + 1, th + 1);// 画边
		g.drawRect(tx - 2, ty - 2, tw + 3, th + 3);// 画边
		g.setColor(0xffffff);
		g.drawString("没有游戏记录,按确认键返回!", tx + 15, ty + 8, TopLeft);
	}
	
	//升级提示可吃的鱼
	private void runPrompt() {
		if (keyState.containsAndRemove(KeyCode.OK)){
			if(own.id == 1 && tollGate == 0){
				timeFlag = System.currentTimeMillis();
				System.out.println(timeFlag);
			}
			imgPrompt2 = null; imgblue = null ;imgyellow = null ; imgbian = null
			; imgbigtummy = null ; imgchangyumiao = null ; imgdolphin = null
			; imgjinyu = null ; imgliyu = null ; imgqiudao = null ; imgshayu =  null ;
			imgshirenyu = null ; imgturtle = null ; imgxiaoyu = null;
			start = 0;
			state = 8; // game
		}
	}


	private void drawPrompt() throws IOException {
		if(imgPrompt2 == null || imgblue == null || imgyellow == null || imgbian == null
				|| imgbigtummy == null || imgchangyumiao == null || imgdolphin == null
				|| imgjinyu == null || imgliyu == null || imgqiudao == null || imgshayu ==  null ||
				imgshirenyu == null || imgturtle == null || imgxiaoyu == null){
			imgPrompt2 = Image.createImage("/prompt2.png");
			imgblue = Image.createImage("/blue.png");
			imgyellow = Image.createImage("/yellow.png");
			imgbian = Image.createImage("/bianyu.png");
			imgbigtummy = Image.createImage("/bigtummy.png");
			imgchangyumiao = Image.createImage("/changyumiao.png");
			imgdolphin = Image.createImage("/dolphin.png");
			imgjinyu = Image.createImage("/jinyu.png");
			imgliyu = Image.createImage("/liyu.png");
			imgqiudao = Image.createImage("/qiudaoyu.png");
			imgshayu = Image.createImage("/shayu.png");
			imgshirenyu = Image.createImage("/shirenyu.png");
			imgturtle = Image.createImage("/turtle.png");
			imgxiaoyu = Image.createImage("/xiaoyumiao.png");
		}
		g.drawImage(imgPrompt2, 75, 126, 0);
		if(own.id == 0){
			g.drawRegion(imgyellow, 0, 0, 60, 26, 0, /*135*/165, 265, 0);//黄鱼
			//g.drawRegion(imgxiaoyu, 0, 0, 50, 20, 0, 235, 268, 0);//小鱼苗
			g.drawRegion(imgchangyumiao, 0, 0, 73, 20, 0, /*315*/280, 270, 0);//长鱼苗
			g.drawRegion(imgliyu, 0, 0, 60, 35, 0, /*420*/405, 263, 0);//鲤鱼
		} else if (own.id == 1){
			g.drawRegion(imgblue, 0, 0, 85, 45, 0, 160, 255, 0);//藍色熱帶魚
			g.drawRegion(imgjinyu, 0, 0, 80, 60, 0, 270, 255, 0);//金魚
			g.drawRegion(imgbigtummy, 0, 0, 90, 49, 0, 385, 255, 0);//大肚魚
		} else if (own.id == 2) {
			g.drawRegion(imgturtle, 0, 0, 105, 60, 0, 120, 255, 0);//海龜
			g.drawRegion(imgbian, 0, 0, 80, 75, 0, 270, 240, 0);//扁魚
			g.drawRegion(imgdolphin, 0, 0, 143, 51, 0, 390, 255, 0);//海豚
		} else if (own.id == 3){
			g.drawRegion(imgqiudao, 0, 0, 100, 38, 0, 125, 270, 0);//秋刀魚
			g.drawRegion(imgshirenyu, 0, 0, 100, 65, 0, 253, 255, 0);//食人魚
			g.drawRegion(imgshayu, 0, 0, 120, 53, 0, 385, 255, 0);//鯊魚
		}
		
		g.drawString("按OK键继续游戏", 260, 400, 0);
	}

	protected void hideNotify() {
		if (state == 8) {// game
			// soundStop(0);
			state = 16;// menu
		} else if (state == 6 || state == 7) {
			noHalt = false;
		}
	}

	protected void showNotify() {
		if (state == 6 || state == 7) {
			noHalt = true;
		}
	}


	private void cleanScreen(SGraphics g, int rgb) {
		g.setClip(0, 0, ScreenW, ScreenH);
		g.setColor(rgb);
		g.fillRect(0, 0, ScreenW, ScreenH);
	}

	private void drawNum(SGraphics g, int num, int x, int y) {
		String number = String.valueOf(num);
		for (byte i = 0; i < number.length(); i++) {
			// int temp = number.charAt(i) - '0';
			// int tx1 = i*(7+1)+x;
			// int tx2 = tx1 - temp*7;
			// g.setClip(tx1,y,7,7);
			// g.drawImage(imgNumber,tx2,y,TopLeft);
			g.drawRegion(imgNumber, (number.charAt(i) - '0') * 18, 0, 18, 18,
					0, x + i * (18 + 1), y, 0);
		}
	}

	// ****************************************************************
	private String str[] = null;
	private int startx, starty, strheight;// strheight-字符串所有行的总高度
	private int start = 0;
	private int wudiFlag = 0 , wudiFlag2 = 0;
	private Image imgProp; //道具

	private void drawStr(SGraphics g, int cx1, int cy1, int cx2, int cy2) {
		// cx1,cy1--裁减起点 cx2,cy2--裁减终点
		tempx = tempy = strheight = 0;
		// g.setFont(Large);
		g.setClip(cx1, cy1, cx2 - cx1, cy2 - cy1);
		g.setColor(255, 255, 255);
		for (int i = 0; i < str.length; ++i) {
			for (int j = 0; j < str[i].length(); ++j) {
				FontW = curFont.charWidth(str[i].charAt(j));
				if ((startx + tempx + FontW) > cx2) {
					tempx = 0;
					tempy += FontH;
					strheight += FontH;
				}
				if ((startx + tempx + FontW) > cx1 && (startx + tempx) < cx2
						&& (starty + tempy + FontH) > cy1
						&& (starty + tempy) < cy2) {
					g.drawChar(str[i].charAt(j), startx + tempx,
							starty + tempy, TopLeft);
				}
				tempx += FontW;
			}
			tempx = 0;
			tempy += FontH;
			strheight += FontH;
		}
		g.setClip(0, 0, ScreenW, ScreenH);
	}

	// ****************************************************************
	public static int attainmentId;
	private void runYidong() {
		
		//isPaidToPlay = purchase.isPaidToPlay(); //判断是不是充值用户
		//isPaidToPlay = true;//demo
		//System.out.println("判断是不是充值用户:" + isPaidToPlay);
	
		// if(System.currentTimeMillis() > timeClock){
		// imgYidong = null;
		// timeClock = System.currentTimeMillis() + 1000;
		coin = new Role();
		coin.frame = 0;
		coin.id = 35;
		state = 3;// main
		java.util.Date gst = new java.util.Date(engineService.getCurrentTime().getTime());
		int year = DateUtil.getYear(gst);
		int month = DateUtil.getMonth(gst);
		attainmentId = year*100+(month+1);
		// }
	}

	private void drawYidong(SGraphics g) throws Exception {
		/*if (imgYidong == null) {
			imgYidong = Image.createImage("/yidong.png");
		}*/
		cleanScreen(g, 0xffffff);
		//g.drawImage(imgYidong, ScreenW >> 1, ScreenH >> 1, HcenterVcenter);
	}

	private void runBbs() {
		if (System.currentTimeMillis() > timeClock) {
			timeClock = System.currentTimeMillis() + 1000;
			coin = new Role();
			coin.frame = 0;
			coin.id = 35;
			state = 3;// main
		}
	}

	private void drawBbs(SGraphics g) throws Exception {
		//if (imgBbs == null) {
			// imgBbs = Image.createImage("/bbs.png");
		//}
		cleanScreen(g, 0xffffff);
		// g.drawImage(imgBbs,ScreenW>>1,ScreenH>>1,HcenterVcenter);
	}

	private void runMain() {
		
		if (keyState.containsAndRemove(KeyCode.BACK)){
			app = 0;
		}
		if (keyState.containsAndRemove(KeyCode.UP)) {// 上
			mainIndex = (mainIndex + 5 - 1) % 5;
		} else if (keyState.containsAndRemove(KeyCode.DOWN)) {// 下
			mainIndex = (mainIndex + 1) % 5;
		} else if (keyState.containsAndRemove(KeyCode.OK)) {// 确定
			// needDrawMainBG = true;
			if (mainIndex == 0) {// 继续游戏
				imgMain1 = imgMain2 = null;
				imgPao1 = imgPao2 = null;
				r = r2 = null;
				selectIndex = 3;
				selectDir = 0;                                                                                                  
				gameRecord.loadGameRecord();
				if (flag == 0){
					state = 20;
				} else {
					state = 26;
					//state = 13;
					//System.out.println("state:"+state);
				}
				//System.out.println("flag:"+flag);	
			} else if (mainIndex == 1) {// 新游戏
				imgMain1 = imgMain2 = null;
				imgPao1 = imgPao2 = null;
				r = r2 = null;
				str = null;
				// state = 6;//intro
				tollGate = 0;
				money = 0;
				scores = 0;
				experience = 0;
				lifeNum = 3;
				start = 1;
				state = 13;
			} else if (mainIndex == 2) {// 游戏排行
				imgMain1 = imgMain2 = null;
				imgPao1 = imgPao2 = null;
				r = r2 = null;
				state = 21;
		        try{
		        	ServiceWrapper sw = this.getServiceWrapper();
		        	rankList = sw.queryRankingList(0, 10);
					if (rankList != null) {
						for (int i = 0; i < rankList.length; ++i) {
							System.out.println(rankList[i].getUserId()+"==>"+rankList[i].getScores());
						}
					} else {
						System.out.println("没有游戏记录!");
					}
		        } catch(Exception e) {
		        	System.out.println("服务器连接失败!");
		        	state = 3;
		        }
				
			} else if (mainIndex == 3) {// 游戏帮助
				imgMain1 = imgMain2 = null;
				imgPao1 = imgPao2 = null;
				r = r2 = null;
				state = 18;// select
			} else if (mainIndex == 4) {// 退出
				imgMain1 = imgMain2 = null;
				imgPao1 = imgPao2 = null;
				r = r2 = null;
				app = 0;
			}
			/*
			 * else if(mainIndex == 1){//载入游戏 imgMain1 = imgMain2 = null;
			 * loadRecord(); selectIndex = 3; selectDir = 0; state = 13;//select
			 * }else if(mainIndex == 2){//设置 state = 4;//sound }
			 */

		}
		r = new Role(); //主界面泡泡
		r2 = new Role();
		r.id = 36;
		r2.id = 37;
		createProp(r);
		createProp(r2);
		propAi();
		//System.out.println("r.id" + r.id);

	}

	// private boolean needDrawMainBG = true;
	private void drawMain(SGraphics g) throws Exception {
		if (imgMain1 == null || imgMain2 == null || imgPao1 == null || imgPao2 == null) {
			imgMain1 = Image.createImage("/main1.jpg");
			imgMain2 = Image.createImage("/main2.png");
			imgPao1 = Image.createImage("/p1.png");
			imgPao2 = Image.createImage("/p2.png");
			// imgCoin = Image.createImage("/coin.png");
		}

		// if (true) {
		g.setClip(0, 0, ScreenW, ScreenH);
		g.drawImage(imgMain1, 0, 0, TopLeft);
		// needDrawMainBG = false;
		// }

		// int menuW = 125, menuH = 39;
		int menuW = 178, menuH = 47;
		int menuAxis[][] = { { 249, 244 }, { 249, 296 }, { 249, 348 },
				{ 249, 400 }, { 249, 452 } };
		for (int i = 0; i < menuAxis.length; ++i) {
			g.drawRegion(imgMain2, (mainIndex != i) ? 0 : menuW, i * menuH,
					menuW, menuH, 0, menuAxis[i][0], menuAxis[i][1], 0);
			
		}
	
		drawProp(g);
		//demo
		/*g.setColor(244, 0, 0);
		g.drawString("欧耶游戏DEMO", 280, 3, TopLeft);*/
		// g.setClip(53,165,70,13);
		// g.drawImage(imgMain2,61,165-mainIndex*13,TopLeft);

		/*
		 * coin.frame = (coin.frame == 0?3:coin.frame-1);
		 * g.setClip(30,165,Para[coin.id][0],Para[coin.id][1]);
		 * g.drawImage(imgCoin
		 * ,30-coin.frame*Para[coin.id][0],165,TopLeft);//画左金币
		 * g.setClip(131,165,Para[coin.id][0],Para[coin.id][1]);
		 * g.drawImage(imgCoin
		 * ,131-coin.frame*Para[coin.id][0],165,TopLeft);//画右金币
		 */
	}

	private void runSound(int tag) {
		/*if ((key & Key02) != 0 || (key & KeyUp) != 0) {
			soundIndex = (soundIndex == 0 ? 0 : soundIndex - 1);
		} else if ((key & Key08) != 0 || (key & KeyDown) != 0) {
			soundIndex = (soundIndex == 1 ? 1 : soundIndex + 1);
		} else if ((key & Key05) != 0 || (key & KeyCenter) != 0) {
			if (soundIndex == 0) {// 声音开
				// soundState = true;
			} else if (soundIndex == 1) {// 声音关
				// soundState = false;
			}
			imgFrm = imgSound = null;
			if (tag == 3) {
				state = 3; // main
				imgCoin = null;
			} else if (tag == 16) {
				state = 16;// menu
			}
		}
		key = 0;*/
	}

	private void drawSound(SGraphics g) throws Exception {
		if (imgFrm == null || imgSound == null || imgCoin == null) {
			imgFrm = Image.createImage("/frm.png");
			imgSound = Image.createImage("/sound.png");
			imgCoin = Image.createImage("/coin.png");
			tempx = (ScreenW >> 1) - 28;
			tempy = (ScreenH >> 1) - 13;
		}
		g.setClip(0, 0, ScreenW, ScreenH);
		g.drawImage(imgFrm, 0, 0, TopLeft);
		g.drawImage(imgSound, tempx, tempy, TopLeft);
		g.setClip(tempx - 18, tempy + soundIndex * 13, 13, 13);
		g.drawImage(imgCoin, tempx - 18 - 13, tempy + soundIndex * 13, TopLeft);// 左
		g.setClip(tempx + 55 + 5, tempy + soundIndex * 13, 13, 13);
		g.drawImage(imgCoin, tempx + 55 + 5 - 39, tempy + soundIndex * 13,
				TopLeft);// 右
	}

	private void runAbout() {
		/*
		 * if((key & Key02) != 0 || (key & Key14) != 0){// if(starty < 4){
		 * starty += FontH; } }else if((key & Key08) != 0 || (key & Key15) !=
		 * 0){// if ( (starty + strheight) > (ScreenH - FontH - 4)) { starty -=
		 * FontH; } }else if((key & Key13) != 0){ str = null; imgCom = null;
		 * state = 3;//main key = 0; }
		 */
	}

	/*
	 * private void drawAbout(SGraphics g) throws Exception{ if(str == null ||
	 * imgCom == null){ imgCom = Image.createImage("/command.png"); String[]
	 * tempstr = { "上海灿宙信息科技有", "限公司出品", "", "www.canzhou.com",
	 * "电话：021-55888060", }; str = tempstr; startx = 6; starty = 40; }
	 * cleanScreen(g,0x0); drawStr(g,4,4,ScreenW-4,ScreenH-FontH-2);
	 * g.setClip(ScreenW-12,ScreenH-12,12,12);
	 * g.drawImage(imgCom,ScreenW,ScreenH,BottomRight); }
	 */

	private void runIntro() {

		if (keyState.containsAndRemove(KeyCode.NUM0)) {
			str = null;
			selectIndex = 3;
			selectDir = 0;
			tollGate = 0;
			state = 13;// select
		}
		starty -= 1;
		if ((starty + strheight) < (ScreenH - FontH - 40)) {
			str = null;
			selectIndex = 3;
			selectDir = 0;
			tollGate = 0;
			state = 13;// select
		}

	}

	private void drawIntro(SGraphics g) {
		if (str == null) {
			String tempstr[] = {
					"  在世界的某一片海域中生活着许多种不同的海洋生物，鱼自然是海洋母亲最宠爱的孩子，他们和海龟、章鱼、以及等等种类的海洋生物生活在一起。而在这片小海域里，生活着一家三口鱼！他们过着非常幸福得生活在自己的家园中。",
					"  一切生活都如此的平静，可是这样的平静却被两条鲨鱼的入侵所打破！小卡伊回到家后看到已经受伤的母亲，眼前的一切都让人不敢相信，母亲告诉他父亲被鲨鱼军团的人抓走了！气愤的小卡伊决定去拯救父亲，但是他的力量实在太小了该如何是好呢？",
					"  危机时刻，一条神秘的神仙鱼出现在他面前，送了一股神气的力量给他，并且告诉他使用这个力量可以将别人的力量变成自己的力量，但是不能用他来做坏事~否则会遭报应。这时的小卡伊为了救自己亲爱的爸爸，毅然决定冒这一次险。并且开始了他救父亲的旅程。。。", };
			str = tempstr;
			startx = 4;
			starty = 80;
		}
		cleanScreen(g, 0x0);
		g.setColor(255, 255, 255);
		g.drawString("跳过", ScreenW, ScreenH, BottomRight);
		drawStr(g, 2, 2, ScreenW - 2, ScreenH - FontH - 4);
	}

	private void runLoad() throws Exception {
	
		load += 1;
		switch (load) {
		case 1:
			imgRole[24] = Image.createImage("/npc_huangyu.png"); //黄鱼
			loadVelocity += 24;// 176为4 128为3
			break;
		case 2:
			imgRole[23] = Image.createImage("/npc_shirenyu.png"); //食人鱼
			loadVelocity += 24;
			break;
		case 3:
			imgRole[22] = Image.createImage("/npc_tortoise1.png");
			loadVelocity += 24;
			break;
		case 4:
			imgRole[21] = Image.createImage("/npc_goldfish.png"); //金鱼
			loadVelocity += 24;
			break;
		case 5:
			imgRole[20] = Image.createImage("/npc_long.png");//长鱼苗
			loadVelocity += 24;
			break;
		case 6:
			imgRole[19] = Image.createImage("/npc_qiudaoyu.png"); //秋刀鱼
			loadVelocity += 24;
			break;
		case 7:
			imgRole[18] = Image.createImage("/npc_bianyu.png"); //扁鱼
			loadVelocity += 24;
			break;
		case 8:
			imgRole[17] = Image.createImage("/npc_bluefish.png");//长鱼苗
			loadVelocity += 24;
			break;
		case 9:
			imgRole[16] = Image.createImage("/npc_liyu.png"); //鲤鱼
			loadVelocity += 24;
			break;
		case 10:
			imgRole[15] = Image.createImage("/npc_fish4.png"); //鲨鱼
			loadVelocity += 24;
			break;
		case 11:
			imgRole[14] = Image.createImage("/npc_dolphin.png");//海豚
			loadVelocity += 24;
			break;
		case 12:
			imgRole[13] = Image.createImage("/npc_bigtummy.png"); //大肚子鱼
			loadVelocity += 24;
			break;
		case 13:
			imgRole[12] = Image.createImage("/npc_xiaoyumiao.png"); //小鱼苗
			loadVelocity += 24;
			break;
		case 14:
			imgKa = Image.createImage("/ka.png");
			loadVelocity += 24;
			break;
		case 15:
			imgPao = Image.createImage("/pao.png");
			loadVelocity += 24;
			break;
		case 16:
			imgProp = Image.createImage("/prop.png");
			loadVelocity += 24;
			break;
		case 17:
			imgDg = Image.createImage("/dg.png");
			loadVelocity += 24;
			break;
		case 18:
			//imgCom = Image.createImage("/command.png");
			loadVelocity += 24;
			break;
		case 19:
			imgInfo = Image.createImage("/info.png");
			loadVelocity += 24;
			break;
		case 20:
			imgLine = Image.createImage("/line.png");
			loadVelocity += 24;
			break;
		case 21:
			imgNumber = Image.createImage("/number.png");
			loadVelocity += 24;
			break;
		case 22:
			imgLevel = Image.createImage("/level.png");
			loadVelocity += 24;
			break;
		case 23:
			imgMap = Image.createImage("/map" + tollGate + ".jpg");
			loadVelocity += 24;
			break;
		case 24:
			imgCoin = Image.createImage("/coin.png");
			// loadVelocity += 0;
			break;
		case 25:
			time = 200;// 使进度条最后一段在画面上能显示
			break;
		default:
			mapw = Gate[tollGate][0] * Gate[tollGate][2];
			maph = Gate[tollGate][1];
			// if(soundState == true){
			// soundPlay(0,-1);
			// }
			state = 8;// game
			time = 100;// 恢复正常速度
			break;
		}
	}

	private void drawLoad(SGraphics g) {
		cleanScreen(g, 0x0);
		g.setColor(0, 156, 90);
		g.drawString("第" + (tollGate + 1) + "关", ScreenW - 20, ScreenH - 30, BottomRight);
		g.drawString("载入中...", 25, ScreenH - 30, BottomLeft);
		g.setColor(128, 128, 0);
		g.drawLine(22, ScreenH - 20, ScreenW - 22, ScreenH - 20);
		g.setColor(128, 180, 0);
		g.drawLine(22, ScreenH - 21, 22 + loadVelocity, ScreenH - 21);
		g.drawLine(22, ScreenH - 22, 22 + loadVelocity, ScreenH - 22);
	}

	private void runGame() {
		if (keyState.containsMoveEventAndRemove(KeyCode.UP)) {// 上
			moveRole(2);
		} else if (keyState.containsMoveEventAndRemove(KeyCode.DOWN)) {// 下
			moveRole(5);
		} else if (keyState.containsMoveEventAndRemove(KeyCode.LEFT)) {// 左
			moveRole(4);
		} else if (keyState.containsMoveEventAndRemove(KeyCode.RIGHT)) {// 右
			moveRole(6);
		} else if (keyState.containsAndRemove(KeyCode.NUM8)) {  //药丸
			if (own.money >= 300) {
				if(own.status != 3 && own.status2 != 11){ //如果是变异状态继续保持
					own.status = 1;
					own.status2 = 0;
					own.money -= 300;
				}
			
			} else {
				state = 27;
			}
		} else if (keyState.containsAndRemove(KeyCode.NUM0)) {  // 菜单
			// soundStop(0);
			menuIndex = 0;
			state = 16;// menu
		} else if (keyState.containsAndRemove(KeyCode.NUM7)) { //无敌状态
			if (own.money >= 300) {
				own.money -= 300;
				//flag2 = 1;
				own.status = 3;
				own.status2 = 11; // 无敌
				own.velocityTime = 150;
				ownBluk = 1; //设置鱼为变异状态,可以吃任何鱼
			} else {
				state = 27;
			}
		} else if (keyState.containsAndRemove(KeyCode.NUM9)){  // 静止
			if (own.money >= 300) {
				own.money -= 300;
				flag3 = 1;
				//flag2 = 1;
				flag4 = 1000;
				/*own.status = 3;
				own.status2 = 11; // 无敌
				own.velocityTime = 500;*/
			}  else {
				state = 27;
			}
		} 
	
		createNpc();
		npcAi();
		propAi();
	
		if (System.currentTimeMillis() > clock2) {
			if (own.velocityNum < Para[own.id][6]) {
				// own.velocityNum += 1;
				clock2 = System.currentTimeMillis() + Para[own.id][7];
			}
		}
	
	}		

	private void drawGame(SGraphics g) {
		
		long timeFlag2 = System.currentTimeMillis();
		if(!isPaidToPlay && own.id == 1 && tollGate == 0 &&  timeFlag2 > timeFlag + 5000){
			state = 28;
		}else{
			if (isDebugMode()) {
				addDebugUserMessage("1键:加经验; 2键:加金币; 3键:生命加1; 4键:屏蔽计费 ;5:打开计费");
			}
			
			if (isRed) {
				cleanScreen(g, 0xff0000);
				isRed = false;
			} else {
				if(own.id == 0 && start == 1){ //重新开始游戏时提示可以吃的鱼
					state = 22;
				}
				drawMap1(g);
				drawProp(g);
				drawNpc(g);
				drawInfo(g);

				if (own.experience >= Para[own.id][3]) {
					own.experience = 0;
					/* maqian modify */
					// TODO :过关/通关
					if (Para[own.id][2] / 2 == 4) {
						if (tollGate == 2) { // 通关
							// soundStop(0);
							state = 11; // win
							timeClock = System.currentTimeMillis() + 2000;
						} else { // 过关
							// soundStop(0);
							start = 1; 
							state = 10; // pass
							timeClock = System.currentTimeMillis() + 2000;
						}
					} else {
						own.id += 1;
						state = 22;
						own.nonceLife = Para[own.id][5];
					}
				}
				// g.setClip(0,ScreenH-4-12,12,12);//左键标记
				// g.drawImage(imgCom,0,ScreenH-4,BottomLeft);
			}
		}
	}
	

	private void createNpc() {

		// tempnum = own.mapx / ScreenW;
		tempnum = own.mapx / Gate[tollGate][0];
		temprole = null;
		for (int i = Limit[tempnum][0].length - 1; i >= 0; i--) {
			if (Confine[Limit[tempnum][0][i]] < Limit[tempnum][1][i]) {
				temprole = new Role();
				temprole.id = Limit[tempnum][0][i];
				// if(temprole.id >= 12 && temprole.id <= 24){
				temprole.status = 1; // 正常游动
				temprole.frame = 0;
				temprole.direction = Math.abs(ran.nextInt() % 2);
				temprole.mapy = Math.abs(ran.nextInt()
						% (ScreenH - Para[temprole.id][1]))
						+ screeny;
				if (temprole.direction == 0) { // 往右
					temprole.mapx = screenx - Para[temprole.id][0] + 1;
				} else { // 往左
					temprole.mapx = screenx + ScreenW - 1;
				}
				temprole.velocityNum = Math.abs(ran.nextInt() % 5); // npc与主角意义不同
				temprole.velocityTime = ran.nextInt() % 15 + 30;
				temprole.isOwn = false;
				Confine[temprole.id] += 1;
				npcs.addElement(temprole);
				// }
			}
		}

	}

	private void npcAi() {
		temprole = null;
		for (int i = 0; i < npcs.size(); ++i) {
			temprole = (Role) (npcs.elementAt(i));
			tempx = temprole.mapx - screenx;
			tempy = temprole.mapy - screeny;
			if ((tempx + Para[temprole.id][0]) > 0 && tempx < ScreenW
					&& (tempy + Para[temprole.id][1]) > 0 && tempy < ScreenH) {
				if (--temprole.velocityTime > 0) {
					if (tollGate == 0) { // 第一关
						freeAi(temprole);// 自由游动
					} else if (tollGate == 1) { // 第二关
						if (Para[temprole.id][2] <= Para[own.id][2]) {
							fleeAi(temprole);// 逃跑
						} else {
							freeAi(temprole);// 自由游动
						}
					} else if (tollGate == 2) { // 第三关
						if (Para[temprole.id][2] <= Para[own.id][2]) {
							fleeAi(temprole);// 逃跑
						} else {
							chaseAi(temprole);// 追
						}
					}
				} else {
					temprole.velocityTime = Math.abs(ran.nextInt() % 20) + 10;
					temprole.velocityNum = Math.abs(ran.nextInt() % 5);
				}
				if (!(eatAi(own, temprole))) { // 主角吃
					eatAi(temprole, own); // 吃主角
					for (int j = 0; j < npcs.size(); ++j) {
						Role temprole2 = (Role) (npcs.elementAt(j));
						try {
							if (!(temprole.equals(temprole2))) {
								eatAi(temprole, temprole2);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				npcs.removeElementAt(i);
				Confine[temprole.id] -= 1;
			}
		}
		if (own.status2 == 11) {// 无敌状态
			if (--own.velocityTime < 0) {
				own.status2 = 0;// 恢复为没有状态,这里指不无敌
				own.status = 1;// 恢复游动
				ownBluk = 0;
			}
		}
		if (own.status2 == 12) {// 生病状态
			if (--own.velocityTime < 0) {
				own.status2 = 0;// 恢复为没有状态,这里指不无敌
				own.status = 1;// 恢复游动
			}
		}
	}

	private void freeAi(Role temprole4) {// 第一关Ai
		if (flag3 != 1) {
			if (temprole4.direction == 0) {// 自由游动
				temprole4.mapx += Speed[temprole4.velocityNum][0];
				temprole4.mapy -= Speed[temprole4.velocityNum][1];
			} else {// 自由游动
				temprole4.mapx -= Speed[temprole4.velocityNum][0];
				temprole4.mapy -= Speed[temprole4.velocityNum][1];
			}
		} else {
			flag4--;
			if (flag4 <= 0) {//时间静止控制
				flag3 = 0;
				//flag2 = 0;
				//own.status2 = 0;// 恢复为没有状态,这里指不无敌
				//own.status = 1;// 恢复游动
			}
			//System.out.println("flag3 : + flag4 :" + flag3 + flag4);
		}
	}

	private void fleeAi(Role temprole4) {// 第二关Ai
		if (flag3 != 1) {
			tempx = Math.abs(temprole4.mapx + (Para[temprole4.id][0] >> 1)
					- own.mapx - (Para[own.id][0] >> 1));
			tempy = Math.abs(temprole4.mapy + (Para[temprole4.id][1] >> 1)
					- own.mapy - (Para[own.id][1] >> 1));
			if (tempx < (Para[own.id][0] + Para[temprole4.id][0])
					&& tempy < Para[own.id][1]) {// 逃跑
				if (temprole4.mapx < own.mapx) {
					temprole4.direction = 1;
					temprole4.mapx -= (Speed[temprole4.velocityNum][0] + 1);
					temprole4.mapy -= Speed[temprole4.velocityNum][1];
				} else {
					temprole4.direction = 0;
					temprole4.mapx += (Speed[temprole4.velocityNum][0] + 1);
					temprole4.mapy -= Speed[temprole4.velocityNum][1];
				}
			} else {// 自由游动
				freeAi(temprole4);
			}
		} else {
			flag4--;
			if (flag4 <= 0) {
				flag3 = 0;
				//flag2 = 0;
				//own.status2 = 0;// 恢复为没有状态,这里指不无敌
				//own.status = 1;// 恢复游动
			}
		}
	}

	private void chaseAi(Role temprole4) {// 第三关Ai
		if (flag3 != 1) {
			tempx = Math.abs(temprole4.mapx + (Para[temprole4.id][0] >> 1)
					- own.mapx - (Para[own.id][0] >> 1));
			tempy = Math.abs(temprole4.mapy + (Para[temprole4.id][1] >> 1)
					- own.mapy - (Para[own.id][1] >> 1));
			tempnum = ran.nextInt() % 2;// 随机追你避免晃动
			if (tempx < (Para[own.id][0] + Para[temprole4.id][0])
					&& tempy < Para[own.id][1] && tempnum == 1) {// 追
				if (temprole4.mapx < own.mapx) {
					temprole4.direction = 0;
					temprole4.mapx += (Speed[temprole4.velocityNum][0] + 2);
					temprole4.mapy -= Speed[temprole4.velocityNum][1];
				} else {
					temprole4.direction = 1;
					temprole4.mapx -= (Speed[temprole4.velocityNum][0] + 2);
					temprole4.mapy -= Speed[temprole4.velocityNum][1];
				}
			} else {// 自由游动
				freeAi(temprole4);
			}
		} else {
			flag4--;
			if (flag4 <= 0) {
				flag3 = 0;
				//flag2 = 0;
				//own.status2 = 0;// 恢复为没有状态,这里指不无敌
				//own.status = 1;// 恢复游动
			}
		}
		
	}

	private boolean eatAi(Role eater, Role sacrifice) {
		if (ownBluk == 1 && (eater.isOwn || sacrifice.isOwn)) { //吃了变异药水之后,主角鱼可以吃任何鱼
			if (eater.isOwn) { 
				if (eater.direction == 0) {// 往右
					if ((eater.mapx + Para[eater.id][0] - 10) >= sacrifice.mapx  
							&& (eater.mapx + Para[eater.id][0] - 10) <= (sacrifice.mapx + Para[sacrifice.id][0])
							&& ((eater.mapy + (Para[eater.id][1] >> 1) + 10) >= sacrifice.mapy
								&& (eater.mapy + (Para[eater.id][1] >> 1) + 10) <= (sacrifice.mapy + Para[sacrifice.id][1])
								&&(eater.mapy + (Para[eater.id][1]/3)) >= sacrifice.mapy
								&& (eater.mapy + (Para[eater.id][1]/3)) <= (sacrifice.mapy + Para[sacrifice.id][1])
								|| (eater.mapy + (Para[eater.id][1]/3) *2) >= sacrifice.mapy
								&& (eater.mapy + (Para[eater.id][1]/3) *2) <= (sacrifice.mapy + Para[sacrifice.id][1]))) {
						if ((sacrifice.id >= 0 && sacrifice.id <= 24)) {
							return eatAi2(eater, sacrifice);
						} else if ((sacrifice.id >= 25 && sacrifice.id <= 28)
								|| sacrifice.id == 33) {
							eatAi3(eater, sacrifice);
						}
					}
				} else {// 往左
					if ((eater.mapx + 10) <= (sacrifice.mapx + Para[sacrifice.id][0])
							&& (eater.mapx + 10) >= sacrifice.mapx 
							&&((eater.mapy + (Para[eater.id][1] >> 1) + 10) >= sacrifice.mapy
								&& (eater.mapy + (Para[eater.id][1] >> 1) + 10) <= (sacrifice.mapy + Para[sacrifice.id][1])
								&&(eater.mapy + (Para[eater.id][1]/3)) >= sacrifice.mapy
								&& (eater.mapy + (Para[eater.id][1]/3)) <= (sacrifice.mapy + Para[sacrifice.id][1])
								|| (eater.mapy + (Para[eater.id][1]/3) *2) >= sacrifice.mapy
								&& (eater.mapy + (Para[eater.id][1]/3) *2) <= (sacrifice.mapy + Para[sacrifice.id][1]))) {
						if (sacrifice.id >= 0 && sacrifice.id <= 24) {
							return eatAi2(eater, sacrifice);
						} else if ((sacrifice.id >= 25 && sacrifice.id <= 28)
								|| sacrifice.id == 33) {
							eatAi3(eater, sacrifice);
						}
					}
				}
			} else if(sacrifice.isOwn){
				if (sacrifice.direction == 0) {// 往右
					if ((sacrifice.mapx + Para[sacrifice.id][0] - 10) >= eater.mapx  
						&& (sacrifice.mapx + Para[sacrifice.id][0] - 10) <= (eater.mapx + Para[eater.id][0])
						&& ((sacrifice.mapy + (Para[sacrifice.id][1] >> 1) + 10) >= eater.mapy
							&& (sacrifice.mapy + (Para[sacrifice.id][1] >> 1) + 10) <= (eater.mapy + Para[eater.id][1])
							&& (sacrifice.mapy + (Para[sacrifice.id][1]/3)) >= eater.mapy
							&& (sacrifice.mapy + (Para[sacrifice.id][1]/3)) <= (eater.mapy + Para[eater.id][1])
							|| (sacrifice.mapy + (Para[sacrifice.id][1]/3) *2) >= eater.mapy
							&& (sacrifice.mapy + (Para[sacrifice.id][1]/3) *2) <= (eater.mapy + Para[eater.id][1]))) {
						if ((eater.id >= 0 && eater.id <= 24)) {
							return eatAi2(sacrifice, eater);
						} else if ((eater.id >= 25 && eater.id <= 28)
								|| eater.id == 33) {
							eatAi3(sacrifice, eater);
						}
					}
				} else {// 往左
					if ((sacrifice.mapx + 10) >= eater.mapx  
						&& (sacrifice.mapx + 10) <= (eater.mapx + Para[eater.id][0])
						&& ((sacrifice.mapy + (Para[sacrifice.id][1] >> 1) + 10) >= eater.mapy
							&& (sacrifice.mapy + (Para[sacrifice.id][1] >> 1) + 10) <= (eater.mapy + Para[eater.id][1])
							&& (sacrifice.mapy + (Para[sacrifice.id][1]/3)) >= eater.mapy
							&& (sacrifice.mapy + (Para[sacrifice.id][1]/3)) <= (eater.mapy + Para[eater.id][1])
							|| (sacrifice.mapy + (Para[sacrifice.id][1]/3) *2) >= eater.mapy
							&& (sacrifice.mapy + (Para[sacrifice.id][1]/3) *2) <= (eater.mapy + Para[eater.id][1]))) {
						if (eater.id >= 0 && eater.id <= 24) {
							return eatAi2(sacrifice, eater);
						} else if ((eater.id >= 25 && eater.id <= 28)
								|| eater.id == 33) {
							eatAi3(sacrifice, eater);
						}
					}
				}
			}
		}  else if (ownBluk == 0){
			if (Para[eater.id][2] >= Para[sacrifice.id][2]) {  //不是变异状态下,主角鱼要和其他的鱼进行比较大小
				if (eater.direction == 0) {// 往右
					if ((eater.mapx + Para[eater.id][0] - 10) >= sacrifice.mapx  
							&& (eater.mapx + Para[eater.id][0] - 10) <= (sacrifice.mapx + Para[sacrifice.id][0])
							&& ((eater.mapy + (Para[eater.id][1] >> 1) + 10) >= sacrifice.mapy
							&& (eater.mapy + (Para[eater.id][1] >> 1) + 10) <= (sacrifice.mapy + Para[sacrifice.id][1])
							||(eater.mapy + (Para[eater.id][1]/3)) >= sacrifice.mapy
							&& (eater.mapy + (Para[eater.id][1]/3)) <= (sacrifice.mapy + Para[sacrifice.id][1])
							|| (eater.mapy + (Para[eater.id][1]/3) *2) >= sacrifice.mapy
							&& (eater.mapy + (Para[eater.id][1]/3) *2) <= (sacrifice.mapy + Para[sacrifice.id][1]) )) {
						if ((sacrifice.id >= 0 && sacrifice.id <= 24)) {
							return eatAi2(eater, sacrifice);
						} else if ((sacrifice.id >= 25 && sacrifice.id <= 28)
								|| sacrifice.id == 33) {
							eatAi3(eater, sacrifice);
						}
					}
				} else {// 往左
					if ((eater.mapx + 10) <= (sacrifice.mapx + Para[sacrifice.id][0])
							&& (eater.mapx + 10) >= sacrifice.mapx 
							&& ((eater.mapy + (Para[eater.id][1] >> 1) + 10) >= sacrifice.mapy
							&& (eater.mapy + (Para[eater.id][1] >> 1) + 10) <= (sacrifice.mapy + Para[sacrifice.id][1])
							||(eater.mapy + (Para[eater.id][1]/3)) >= sacrifice.mapy
							&& (eater.mapy + (Para[eater.id][1]/3)) <= (sacrifice.mapy + Para[sacrifice.id][1])
							|| (eater.mapy + (Para[eater.id][1]/3) *2) >= sacrifice.mapy
							&& (eater.mapy + (Para[eater.id][1]/3) *2) <= (sacrifice.mapy + Para[sacrifice.id][1]) )) {
						if (sacrifice.id >= 0 && sacrifice.id <= 24) {
							return eatAi2(eater, sacrifice);
						} else if ((sacrifice.id >= 25 && sacrifice.id <= 28)
								|| sacrifice.id == 33) {
							eatAi3(eater, sacrifice);
						}
					}
				}
			}
		}
	 	return false;
   }

	private boolean eatAi2(Role eater2, Role sacrifice2) {// 吃鱼
		boolean b = false;
		if (eater2.equals(own)) {// 吃者为主角
			if (eater2.status2 != 12) {// 生病
				eater2.money += Para[sacrifice2.id][5];
				eater2.experience += Para[sacrifice2.id][3];
				eater2.scores += eater2.experience;
				System.out.println("scores: "+eater2.scores);
				
				/*if (eater2.experience >= Para[eater2.id][3]) {
					eater2.experience = 0;
					if (Para[eater2.id][2] / 2 == 4) {
						if (tollGate == 2) { // 通关 //soundStop(0); state = 11;
												// //win
							timeClock = System.currentTimeMillis() + 2000;
						} else { // 过关
							// soundStop(0);
							state = 10; // pass
							timeClock = System.currentTimeMillis() + 2000;
						}
					} else {
						eater2.id += 1;
						own.lifeNum = Para[eater2.id][5];
					}
				}*/
				 
				createProp(sacrifice2);
				if (eater2.status2 == 11) {
					eater2.status = 5;// 无敌吃(只有主角有)
				} else {
					eater2.status = 2; // 游动吃
				}

				if (++eatNum > 40) {
					eatNum = 0;
					if (Math.abs(ran.nextInt() % 2) == 1) {
						eater2.status = 6; // 生病
						eater2.status2 = 12; // 生病
						eater2.velocityTime = 300;
					}
				}

				b = true;
				npcs.removeElement(sacrifice2);
				Confine[sacrifice2.id] -= 1;
				return b;
			}
		} else {
			if (sacrifice2.equals(own)) { // 被吃者为主角
			/*	if(flag2 == 1){ //无敌效果
					sacrifice2.status = 3;
					sacrifice2.status2 = 11; // 无敌
					sacrifice2.velocityTime = 100;
					flag2 = 0;
				}*/
				if (sacrifice2.status2 != 11) { // 受伤后不能连续受伤
					sacrifice2.nonceLife -= (Para[eater2.id][4] / Para[sacrifice2.id][8]);
					isRed = true; // 受伤时红屏标记
					sacrifice2.status = 3;
					sacrifice2.status2 = 11; // 无敌
				    sacrifice2.velocityTime = 30; // 受伤后无敌维持时间
					if (sacrifice2.nonceLife <= 0) {
						state = 14; // dead
						own.status = 4;
					}
				}
			} else {
				npcs.removeElement(sacrifice2);
				Confine[sacrifice2.id] -= 1;
			}
			/* maqian add 20100926 */
			eater2.status = 2; // 游动吃
		}
		return b;
	}

	private void eatAi3(Role eater, Role sacrifice3) {// 吃道具
		if (eater.equals(own) && eater.status2 != 12) {
			if (sacrifice3.id == 25) {
				if (levin == null) {
					levin = new Role();
				}
				levin.id = 30; // 电光
				levin.velocityNum = 2; // 跳动次数
				levin.velocityTime = 3; // 每次持续时间
				levin.frame = 0;
				levin.mapx = Math.abs(ran.nextInt()
						% (ScreenW - Para[levin.id][0]))
						+ screenx;
				levin.mapy = -Math
						.abs(ran.nextInt() % (Para[levin.id][1] >> 1))
						+ screeny;
				state = 9; // dg
			} else if (sacrifice3.id == 26) { //五角星
				eater.lifeNum += 1; // 增加生命1条
			} else if (sacrifice3.id == 27) { //
				eater.nonceLife += 50; // 增加50点血
				if (eater.nonceLife > Para[own.id][5]) {
					eater.nonceLife = Para[own.id][5];
				}
			} else if (sacrifice3.id == 28) {
				eater.nonceLife += 100; // 增加100点血
				if (eater.nonceLife > Para[own.id][5]) {
					eater.nonceLife = Para[own.id][5];
				}
			} else if (sacrifice3.id == 33) {
				sacrifice3.status = 0;// 珍珠被吃
				eater.money += Para[sacrifice3.id][3];
				return;
			}
			props.removeElement(sacrifice3);
		}
	}

	private void runPWO() {
		if (System.currentTimeMillis() > timeClock) {
			if (state == 10) {// pass
				tollGate += 1;
				state = 13; // select
				scores = own.scores;
				money = own.money + 500;
				lifeNum = own.lifeNum;
			
			} else if (state == 11) {// win
				scores = own.scores;
				money = own.money;
				lifeNum = own.lifeNum;
				flag = 1; //标志为没有游戏记录
				state = 21;// rankList
				//GameAttainment gamerecord = attainmentServ.read(accountId, productId, attainmentId);
				//rank = gamerecord.getRanking();
				ServiceWrapper sw = this.getServiceWrapper();
	        	rankList = sw.queryRankingList(0, 10);
				if (rankList != null) {
					for (int i = 0; i < rankList.length; ++i) {
						System.out.println(rankList[i].getUserId()+"==>"+rankList[i].getScores());
					}
				}
			} else if (state == 12) {// over
				state = 3;// main
				isRed = false;
			}
			imgCoin = imgKa = imgPao = imgGu = imgDg = imgLine = null;
			imgPearl = imgWalk = imgMap = imgInfo = imgLevel = imgNumber = null;
			imgPrompt = foot2 = imgProp = imgEye = imgFish = null;
			for (int i = imgRole.length - 1; i >= 0; --i) {
				imgRole[i] = null;
			}
			for (int i = wings.length - 1; i >= 0; --i) {
				wings[i] = null;
			}
			for (int i = eyes.length - 1; i >= 0; --i) {
				eyes[i] = null;
			}
		}
	}

	private void drawPWO(SGraphics g) {// pass,win,over
		String str1 = null;
		switch (state) {
		case 10:// pass
			str1 = "游戏过关,奖励500金币";
			break;
		case 11:// win
			str1 = "游戏胜利";
			break;
		case 12:// over
			str1 = "游戏结束";
			break;
		}
		int tw = curFont.stringWidth(str1) + 30;
		int th = FontH + 20;
		int tx = ScreenW / 2 - tw / 2;
		int ty = ScreenH / 2 - th;
		g.setClip(0, 0, ScreenW, ScreenH);
		g.setColor(0, 78, 157);
		g.fillRect(tx, ty, tw, th);// 画框
		g.setColor(243, 191, 99);
		g.drawRect(tx - 1, ty - 1, tw + 1, th + 1);// 画边
		g.drawRect(tx - 2, ty - 2, tw + 3, th + 3);// 画边
		g.setColor(Math.abs(ran.nextInt() % 0xffffff));
		g.drawString(str1, tx + 15, ty + 10, TopLeft);
	}

	private void createProp(Role sign) {
		Role temprole3;
		if (sign == null) {
			/*if (ran.nextInt() % 21 == 1) {
				temprole3 = new Role();
				temprole3.id = 29; // 泡
				temprole3.mapx = Math.abs(ran.nextInt()
						% (ScreenW - Para[temprole3.id][0]))
						+ screenx;
				temprole3.mapy = ScreenH - 1 + screeny;
				temprole3.frame = 0;
				temprole3.velocityNum = 0;// 控制泡抖动
				temprole3.velocityTime = 3;// 持续显示时间
				props.addElement(temprole3);
			}*/
		} else if (sign.id == 24) {// 黄鱼
			tempnum = Math.abs(ran.nextInt() % 150);
			//System.out.println("tempnum:"+tempnum);
			if (tempnum <= 25) {
				temprole3 = new Role();
				if (tempnum <= 5) { // 生命药
					temprole3.id = 26;
				} else if (tempnum <= 10) { // 闪电
					temprole3.id = 25;
				} else if (tempnum <= 20) { // 小血药
					temprole3.id = 27;
				} else if (tempnum <= 25) { // 大血药
					 temprole3.id = 28; 
				}
				 
				temprole3.mapx = Math.abs(ran.nextInt()
						% (ScreenW - Para[temprole3.id][0]))
						+ screenx;
				temprole3.mapy = screeny + ScreenH - 1;
				temprole3.isOwn = false;
				props.addElement(temprole3);
			}
		} else if(sign.id == 36){
			if (ran.nextInt() % 21 == 1) {
				temprole3 = new Role();
				temprole3.id = 36; // 泡
				temprole3.mapx = Math.abs(ran.nextInt()
						% (ScreenW - Para[temprole3.id][0]))
						+ screenx;
				temprole3.mapy = ScreenH - 1 + screeny;
				temprole3.frame = 0;
				temprole3.velocityNum = 0;// 控制泡抖动
				temprole3.velocityTime = 3;// 持续显示时间
				temprole3.isOwn = false;
				props.addElement(temprole3);
				//System.out.println("temprole3.mapx" + temprole3.mapx);
				//System.out.println("temprole3.mapy" + temprole3.mapy);
			}
		} else if (sign.id == 37){
			if (ran.nextInt() % 21 == 1) {
				temprole3 = new Role();
				temprole3.id = 37; // 泡
				temprole3.mapx = Math.abs(ran.nextInt()
						% (ScreenW - Para[temprole3.id][0]))
						+ screenx;
				temprole3.mapy = ScreenH - 1 + screeny;
				temprole3.frame = 0;
				temprole3.velocityNum = 0;// 控制泡抖动
				temprole3.velocityTime = 3;// 持续显示时间
				temprole3.isOwn = false;
				props.addElement(temprole3);
				//System.out.println("temprole3.mapx" + temprole3.mapx);
				//System.out.println("temprole3.mapy" + temprole3.mapy);
			}
		} else {
			temprole3 = new Role();
			temprole3.id = 31;// 咔
			temprole3.velocityTime = 2;
			temprole3.mapx = sign.mapx;
			temprole3.mapy = sign.mapy;
			temprole3.isOwn = false;
			props.addElement(temprole3);
			/*
			 * temprole3 = new Role(); temprole3.id = 32;//骨头 temprole3.mapx =
			 * sign.mapx; temprole3.mapy = sign.mapy; temprole3.frame =
			 * Math.abs(ran.nextInt())%2; props.addElement(temprole3);
			 */
		}
	}

	private void propAi() {
		temprole = null;
		for (int i = 0; i < props.size(); ++i) {
			temprole = (Role) (props.elementAt(i));
			if (temprole.id == 34) {// 贝壳
				if (temprole.frame == 0 && temprole.cahoot.status == 0) { // 闭者
					if (ran.nextInt() % 60 == 1) {
						temprole.cahoot.status = 1; // 生成贝壳
					}
				}
				continue;
			}
			if (temprole.id == 33) {// 珍珠
				if (temprole.status == 1 && temprole.cahoot.frame == 1) {
					eatAi(own, temprole);
				}
				continue;
			}

			tempx = temprole.mapx - screenx;
			tempy = temprole.mapy - screeny;
			if ((tempx + Para[temprole.id][0]) > 0 && tempx < ScreenW
					&& (tempy + Para[temprole.id][1]) > 0 && tempy < ScreenH) {
				if (temprole.id >= 25 && temprole.id <= 28) {// 四种道具
					temprole.mapy -= 2;
					eatAi(own, temprole);
				} else if (temprole.id == 31) {// 咔
					if (--temprole.velocityTime < 0) {
						props.removeElementAt(i);
					}
				} else if (temprole.id == 32) {// 骨头
					temprole.mapy += 2;
				} else if (temprole.id == 29) {// 泡
					// if(temprole.velocityNum == 0){
					// temprole.mapx += 2;
					// temprole.velocityNum = 1;
					// }else{
					// temprole.mapx -= 2;
					// temprole.velocityNum = 0;
					// }
					temprole.mapy -= 5;
				} else if (temprole.id == 37){
					temprole.mapy -= 5;
				} else if (temprole.id == 36){
					temprole.mapy -= 5;
				}
			} else {
				props.removeElementAt(i);
			}
		}
	}

	private void drawNpc(SGraphics g) {
		try {
			temprole = null;
			g.setClip(0, 0, ScreenW, ScreenH);
			for (int i = npcs.size() - 1; i >= 0; --i) {
				temprole = (Role) (npcs.elementAt(i));
				tempx = temprole.mapx - screenx;
				tempy = temprole.mapy - screeny;
				if (temprole.status == 1) {// 正常游动
					if (temprole.id == 22) { // 乌龟的状态
						temprole.frame = 0;
					} else {
						temprole.frame = (temprole.frame + 1) % 4;
					}
				} else if (temprole.status == 2) {// 吃
					if (temprole.id == 15) { // 判断鲸鱼吃的状态
						temprole.frame = 2;
						flag2Index++;
						if (flag2Index >= 3) {
							temprole.status = 1;// 恢复游动
							flag2Index = 0;
						}
					} else {
						if (temprole.id == 22) { // 乌龟吃的状态
							temprole.frame = 1;
						} else { // 其他鱼吃的状态
							temprole.frame = 4;
						}

						temprole.status = 1;// 恢复游动
					}
				}
				if (temprole.id == 15) {//画鲸鱼
					if( imgEye == null || imgFish == null ){
						 imgFish = Image.createImage("/npc_fish4_2.png");
						 imgEye = Image.createImage("/eye.png");
					}
					if(temprole.status == 2){ //吃的状态
						g.drawRegion(imgRole[temprole.id], (temprole.frame + flag2Index + 6) * Para[temprole.id][0], 0,
								Para[temprole.id][0], Para[temprole.id][1],	temprole.direction == 0 ? Sprite.TRANS_MIRROR
										: 0, tempx, tempy, 0);
						if(temprole.direction != 0){
					        g.drawRegion(imgFish, fishIndex * 69, 0, 69, 47,temprole.direction == 0 ? Sprite.TRANS_MIRROR
								: 0, tempx + 76, tempy + 45, 0);
						} else {
							 g.drawRegion(imgFish, fishIndex * 69, 0, 69, 47,temprole.direction == 0 ? Sprite.TRANS_MIRROR
										: 0, tempx + Para[temprole.id][0] - 145, tempy + 45, 0);
						}
					    if(loopIndex == 0){ //控制鲸鱼翅膀和尾巴的摇摆的频率
							loopIndex ++;
						} else{
							fishIndex = (fishIndex + 1) % 5;
						    loopIndex = 0;
						    countIndex = (countIndex + 1) % 9 ;
						}
					} else { //游动的状态
						if (countIndex <= 6) {
							g.drawRegion(imgRole[temprole.id], countIndex * Para[temprole.id][0], 0,
										Para[temprole.id][0], Para[temprole.id][1],	temprole.direction == 0 ? Sprite.TRANS_MIRROR
												: 0, tempx, tempy, 0);
							if(temprole.direction != 0){
						        g.drawRegion(imgFish, fishIndex * 69, 0, 69, 47,temprole.direction == 0 ? Sprite.TRANS_MIRROR
									: 0, tempx + 76, tempy + 45, 0);
						        g.drawRegion(imgEye, eyeIndex * 13, 0, 13, 13,0, tempx + 22, tempy + 51, 0);
							} else {
								 g.drawRegion(imgFish, fishIndex * 69, 0, 69, 47,temprole.direction == 0 ? Sprite.TRANS_MIRROR
											: 0, tempx + Para[temprole.id][0] - 145, tempy + 45, 0);
								 g.drawRegion(imgEye, eyeIndex * 13, 0, 13, 13,0, tempx + Para[temprole.id][0] - 36, tempy + 51, 0);
							}
						    if(loopIndex == 0){ //控制鲸鱼翅膀和尾巴的摇摆的频率
								loopIndex ++;
							} else{
								fishIndex = (fishIndex + 1) % 5;
							    loopIndex = 0;
							    countIndex = (countIndex + 1) % 9 ;
							}
						    if(flagIndex <= 8){//控制鲸鱼眼睛眨的频率
						    	flagIndex ++;
						    	eyeIndex = 1;
						    } else {
						    	flagIndex = 1;
						    	eyeIndex = 0;
						    }
						
						} else {
							loopIndex = 0;
							flagIndex = 1;
							g.drawRegion(imgRole[temprole.id], countIndex * Para[temprole.id][0], 0,
										Para[temprole.id][0], Para[temprole.id][1],	temprole.direction == 0 ? Sprite.TRANS_MIRROR
												: 0, tempx, tempy, 0);
							if(temprole.direction != 0){
						        g.drawRegion(imgFish, fishIndex * 69, 0, 69, 47,temprole.direction == 0 ? Sprite.TRANS_MIRROR
									: 0, tempx + 76, tempy + 45, 0);
						        g.drawRegion(imgEye, eyeIndex * 13, 0, 13, 13,0, tempx + 22, tempy + 51, 0);
							} else {
								 g.drawRegion(imgFish, fishIndex * 69, 0, 69, 47,temprole.direction == 0 ? Sprite.TRANS_MIRROR
											: 0, tempx + Para[temprole.id][0] - 145, tempy + 45, 0);
								 g.drawRegion(imgEye, eyeIndex * 13, 0, 13, 13,0, tempx + Para[temprole.id][0] - 36, tempy + 51, 0);
							}
							countIndex = (countIndex + 1) % 9 ;
							fishIndex = (fishIndex + 1) % 5;
							if (flagIndex <= 8) {  //控制鲸鱼眼睛眨的频率
								flagIndex++;
								eyeIndex = 1;
							} else {
								flagIndex = 1;
								eyeIndex = 0;
							}
						}
					}
					
				} else if(temprole.id == 22 ){ //画乌龟
					if(wings[temprole.id - 12] == null || foot2 == null || eyes[temprole.id - 12] == null ){
						wings[10] = Image.createImage("/npc_tortoise1_foot.png"); //乌龟前腿
						foot2 = Image.createImage("/npc_tortoise1_foot2.png"); //乌龟后腿
						eyes[10] = Image.createImage("/npc_tortoise1_eye.png");
					}
					g.drawRegion(imgRole[temprole.id], temprole.frame * Para[temprole.id][0], 0,
							Para[temprole.id][0], Para[temprole.id][1],	temprole.direction == 0 ? Sprite.TRANS_MIRROR
									: 0, tempx, tempy, 0);
					if(temprole.direction != 0){
				        g.drawRegion(wings[temprole.id - 12], tortoiseIndex * 47, 0, 47, 44,temprole.direction == 0 ? Sprite.TRANS_MIRROR
							: 0, tempx + footCoordinate[temprole.id - 22][0], tempy + footCoordinate[temprole.id - 22][1], 0);
				        g.drawRegion(foot2, tortoise2Index * 32, 0, 32, 24,temprole.direction == 0 ? Sprite.TRANS_MIRROR
								: 0, tempx + foot2Coordinate[temprole.id - 22][0], tempy + foot2Coordinate[temprole.id - 22][1], 0);
				        g.drawRegion(eyes[temprole.id - 12], teyeIndex * 6, 0, 6, 6,temprole.direction == 0 ? Sprite.TRANS_MIRROR
								: 0, tempx + eyeCoordinate[temprole.id - 22][0], tempy + eyeCoordinate[temprole.id - 22][1], 0);
					} else {
						 g.drawRegion(wings[temprole.id - 12], tortoiseIndex * 47, 0, 47, 44,temprole.direction == 0 ? Sprite.TRANS_MIRROR
									: 0, tempx + Para[temprole.id][0] - footCoordinate[temprole.id - 22 + 1][0], tempy + footCoordinate[temprole.id - 22][1], 0);
						 g.drawRegion(foot2, tortoise2Index * 32, 0, 32, 24,temprole.direction == 0 ? Sprite.TRANS_MIRROR
									: 0, tempx + Para[temprole.id][0] - foot2Coordinate[temprole.id - 22 + 1][0], tempy + foot2Coordinate[temprole.id - 22][1], 0);
						 g.drawRegion(eyes[temprole.id - 12], teyeIndex * 6, 0, 6, 6,temprole.direction == 0 ? Sprite.TRANS_MIRROR
									: 0, tempx + Para[temprole.id][0] - eyeCoordinate[temprole.id - 22 + 1][0], tempy + eyeCoordinate[temprole.id - 22][1], 0);
					}
					if(tortoiseflag <= 3){ //控制乌龟腿的移动
						tortoiseflag++;
					} else{
						tortoiseIndex = (tortoiseIndex + 1) % 4;
						tortoise2Index = (tortoise2Index + 1) % 5;
						tortoiseflag = 0;
					}
					
					if (own2Index <= 6) {  //控制乌龟眼睛眨的频率
						own2Index++;
						teyeIndex = 0;
					} else {
						own2Index = 1;
						teyeIndex = 1;
					}
				} else if (temprole.id == 24 || temprole.id == 23 || temprole.id == 19 || temprole.id == 18 || temprole.id == 16 
						|| temprole.id == 13 || temprole.id == 14 || temprole.id == 20 || temprole.id == 21 || temprole.id == 17 ){ //画配角鱼
					//鱼身的其他部位
					if(temprole.id == 24 && wings[12] == null && eyes[12] == null){
						wings[12] = Image.createImage("/npc_huangyu_wing.png");
						eyes[12] = Image.createImage("/npc_huangyu_eye.png");
					} else if(temprole.id == 23 && wings[11] == null && eyes[11] == null){
						wings[11] = Image.createImage("/npc_shirenyu_wing.png");
						eyes[11] = Image.createImage("/npc_shirenyu_eye.png");
					} else if(temprole.id == 22 && wings[10] == null && eyes[10] == null){
						wings[10] = Image.createImage("/npc_tortoise1_foot.png"); //乌龟前腿
						foot2 = Image.createImage("/npc_tortoise1_foot2.png"); //乌龟后腿
						eyes[10] = Image.createImage("/npc_tortoise1_eye.png");
					} else if(temprole.id == 21 && wings[9] == null && eyes[9] == null){
						wings[9] = Image.createImage("/npc_goldfish_wing.png");
						eyes[9] = Image.createImage("/npc_goldfish_eye.png");
					} else if(temprole.id == 20 && wings[8] == null && eyes[8] == null){
						wings[8] = Image.createImage("/npc_long_wing.png");
						eyes[8] = Image.createImage("/npc_long_eye.png");
					} else if(temprole.id == 19 && wings[7] == null && eyes[7] == null){
						wings[7] = Image.createImage("/npc_qiudaoyu_wing.png");
						eyes[7] = Image.createImage("/npc_qiudaoyu_eye.png");
					} else if(temprole.id  == 18 && wings[6] == null && eyes[6] == null){
						wings[6] = Image.createImage("/npc_bianyu_wing.png");
						eyes[6] = Image.createImage("/npc_bianyu_eye.png");
					} else if(temprole.id == 17 && wings[5] == null && eyes[5] == null){
						wings[5] = Image.createImage("/npc_bluefish_wing.png");
						eyes[5] = Image.createImage("/npc_bluefish_eye.png");
					} else if(temprole.id == 16 && wings[4] == null && eyes[4] == null){
						wings[4] = Image.createImage("/npc_liyu_wing.png");
						eyes[4] = Image.createImage("/npc_liyu_eye.png");
					} else if(temprole.id == 14 && wings[2] == null && eyes[2] == null){
						wings[2] = Image.createImage("/npc_dolphin_wing.png");
						eyes[2] = Image.createImage("/npc_dolphin_eye.png");
					} else if(temprole.id == 13 && wings[1] == null && eyes[1] == null){
						wings[1] = Image.createImage("/npc_bigtummy_wing.png");
						eyes[1] = Image.createImage("/npc_bigtummy_eye.png");
					}
					
					 g.drawRegion(imgRole[temprole.id], temprole.frame * Para[temprole.id][0], 0, Para[temprole.id][0],
							Para[temprole.id][1], temprole.direction == 0 ? Sprite.TRANS_MIRROR : 0, tempx, tempy, 0);
					if (temprole.direction != 0) {
						g.drawRegion(wings[temprole.id  - 12], wingIndex * npc[temprole.id - 12][2], 0, npc[temprole.id - 12][2], npc[temprole.id - 12][3],
								temprole.direction == 0 ? Sprite.TRANS_MIRROR : 0, tempx + npc[temprole.id - 12][6], tempy + npc[temprole.id - 12][7], 0);
						g.drawRegion(eyes[temprole.id - 12], Para[temprole.id][6] * npc[temprole.id - 12][0], 0, npc[temprole.id - 12][0], npc[temprole.id - 12][1], 
								0, tempx + npc[temprole.id - 12][4], tempy + npc[temprole.id - 12][5], 0);
					} else {
						g.drawRegion(wings[temprole.id  - 12], wingIndex * npc[temprole.id - 12][2], 0, npc[temprole.id - 12][2], npc[temprole.id - 12][3], 
								temprole.direction == 0 ? Sprite.TRANS_MIRROR : 0, tempx + Para[temprole.id][0] - npc[temprole.id - 12][10], tempy + npc[temprole.id - 12][11],	0);
						g.drawRegion(eyes[temprole.id - 12], Para[temprole.id][6] * npc[temprole.id - 12][0],	0, 
								npc[temprole.id - 12][0], npc[temprole.id - 12][1], 0, tempx	+ Para[temprole.id][0] - npc[temprole.id - 12][8], tempy + npc[temprole.id - 12][9], 0);
					}
					
					if(loop2Index == 0){ //控制鱼翅膀频率
						loop2Index ++;
					} else{
						wingIndex = (wingIndex + 1) % 3;
					    loop2Index = 0;
					}
					
					if (Para[temprole.id][7] <= 8) {  //控制鱼眼睛眨的频率
						Para[temprole.id][7]++;
						Para[temprole.id][6] = 0;
					} else {
						Para[temprole.id][7] = 1;
						Para[temprole.id][6] = 1;
					}
					
				} /*else if (temprole.id == 12) { //小鱼苗
					if(eyes[temprole.id - 12] == null ){
						eyes[temprole.id - 12] = Image.createImage("npc_xiaoyumiao_eye.png");
					}
					g.drawRegion(imgRole[temprole.id], temprole.frame * Para[temprole.id][0], 0, Para[temprole.id][0],
							Para[temprole.id][1], temprole.direction == 0 ? Sprite.TRANS_MIRROR : 0, tempx, tempy, 0);
					if(temprole.direction != 0){
						g.drawRegion(eyes[temprole.id - 12], e2Index * npc[temprole.id - 12][0], 0, npc[temprole.id - 12][0], npc[temprole.id - 12][1], 
								0, tempx + npc[temprole.id - 12][4], tempy + npc[temprole.id - 12][5], 0);
					} else {
						g.drawRegion(eyes[temprole.id - 12], e2Index * npc[temprole.id - 12][0],
								0, npc[temprole.id - 12][0], npc[temprole.id - 12][1], 0, tempx	+ Para[temprole.id][0] - npc[temprole.id - 12][8], tempy + npc[temprole.id - 12][9], 0);
					}
					
					if (own4Index <= 6) {  //控制鱼眼睛眨的频率
						own4Index++;
						e2Index = 0;
					} else {
						own4Index = 1;
						e2Index = 1;
					}
					
				} */
				/*else { //画其他的鱼
					//g.drawRegion(imgRole[temprole.id], Para[temprole.id][0], 0, Para[temprole.id][0],
							//Para[temprole.id][1], temprole.direction == 0 ? 0 : Sprite.TRANS_MIRROR, tempx, tempy, TopLeft);
				}*/
			}
			if (imgRole[own.id] == null || imgWudi[own.id] == null || imgOwneye == null || imgOwnwing == null ) {
				imgRole[0] = imgRole[1] = imgRole[2] = imgRole[3] = null;
				imgRole[own.id] = Image.createImage(hero[own.id]);
				imgOwnwing = Image.createImage(heroWing[own.id]);
				imgOwneye = Image.createImage(heroEye[own.id]);
				imgWudi[own.id] = Image.createImage(wudi[own.id]);
			}
			tempx = own.mapx - screenx;
			tempy = own.mapy - screeny;
			if (own.status == 1) {// 正常游动
				own.frame = (own.frame + 1) % 3;
			} else if (own.status == 2) {// 游动时吃
				own.frame = 4;
				own.status = 1;// 恢复游动
			} else if (own.status == 3) {// 无敌
				own.frame = (own.frame == 0 ? 3 : 0);
			} else if (own.status == 4) {// 死亡
				own.frame = 6;
			} else if (own.status == 5) {// 无敌时吃
				own.frame = 4;
				own.status = 3;// 恢复游动
			} else if (own.status == 6) {// 生病
				own.frame = (own.frame == 0 ? 5 : 0);
			}

			// g.setClip(tempx, tempy, Para[own.id][0], Para[own.id][1]);
			/*
			 * g.drawImage(imgRole[own.id], tempx - own.frame * Para[own.id][0],
			 * tempy - own.direction * Para[own.id][1], TopLeft);
			 */
            //主角鱼的合成
		
			if (own.status != 6 && own.status != 4){
				g.drawRegion(imgRole[own.id], own.frame * Para[own.id][0], 0,  //身体
						Para[own.id][0], Para[own.id][1], own.direction == 0 ? Sprite.TRANS_MIRROR
								: 0, tempx, tempy, TopLeft);
				if(own.direction != 0){
					
			        g.drawRegion(imgOwneye, owneyeIndex * (imgOwneye.getWidth()/2), 0, imgOwneye.getWidth()/2, imgOwneye.getHeight(),own.direction == 0 ? Sprite.TRANS_MIRROR
						: 0, tempx + coordinate[own.id][0], tempy + coordinate[own.id][1], TopLeft);  //眼睛
			        g.drawRegion(imgOwnwing, ownwingIndex * (imgOwnwing.getWidth()/3), 0, imgOwnwing.getWidth()/3, imgOwnwing.getHeight(),own.direction == 0 ? Sprite.TRANS_MIRROR
							: 0, tempx + coordinate2[own.id][0], tempy + coordinate2[own.id][1], TopLeft); //翅膀
			        if(own.status == 3){ //无敌时的圈圈
						g.drawRegion(imgWudi[own.id], wudiFlag2 *(imgWudi[own.id].getWidth()/4), 0, imgWudi[own.id].getWidth()/4, imgWudi[own.id].getHeight(),
								0, tempx - wudicoordinate[own.id][0], tempy - wudicoordinate[own.id][1], TopLeft);
						if(wudiFlag < 0){
							wudiFlag ++;
						} else {
							wudiFlag = 0;
							wudiFlag2 = (wudiFlag2 + 1) % 4 ;
						}
					}
				} else {
					
					 g.drawRegion(imgOwneye, owneyeIndex * (imgOwneye.getWidth()/2), 0, imgOwneye.getWidth()/2, imgOwneye.getHeight(),own.direction == 0 ? Sprite.TRANS_MIRROR
								: 0, tempx + Para[own.id][0] - coordinate[own.id + 4][0], tempy + coordinate[own.id + 4][1], TopLeft);
					 g.drawRegion(imgOwnwing, ownwingIndex * (imgOwnwing.getWidth()/3), 0, imgOwnwing.getWidth()/3, imgOwnwing.getHeight(),own.direction == 0 ? Sprite.TRANS_MIRROR
								: 0, tempx + Para[own.id][0] - coordinate2[own.id + 4][0], tempy + coordinate2[own.id + 4][1], TopLeft);
					 if(own.status == 3){ //反向无敌时的圈圈
							g.drawRegion(imgWudi[own.id], wudiFlag2 *(imgWudi[own.id].getWidth()/4), 0, imgWudi[own.id].getWidth()/4, imgWudi[own.id].getHeight(),
									0, tempx - wudicoordinate[own.id + 4][0], tempy - wudicoordinate[own.id + 4][1], TopLeft);
							if(wudiFlag < 0){
								wudiFlag ++;
							} else {
								wudiFlag = 0;
								wudiFlag2 = (wudiFlag2 + 1) % 4 ;
							}
						}
				}
				if(loop2Index == 0){ //控制主角鱼翅膀和尾巴的摇摆的频率
					loop2Index ++;
				} else{
					ownwingIndex = (ownwingIndex + 1) % 3;
				    loop2Index = 0;
				}
				
				if (ownIndex <= 2) {  //控制主鱼眼睛眨的频率
					ownIndex++;
					owneyeIndex = 0;
				} else {
					ownIndex = 1;
					owneyeIndex = 1;
				}
			} else { //死亡和生病
				g.drawRegion(imgRole[own.id], own.frame * Para[own.id][0], 0,  //身体
						Para[own.id][0], Para[own.id][1], own.direction == 0 ? Sprite.TRANS_MIRROR
								: 0, tempx, tempy, TopLeft);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void drawProp(SGraphics g) {
		temprole = null;
		g.setClip(0, 0, ScreenW, ScreenH);
		for (int i = props.size() - 1; i >= 0; --i) {
			temprole = (Role) (props.elementAt(i));
			tempx = temprole.mapx - screenx;
			tempy = temprole.mapy - screeny;
			// g.setClip(tempx,tempy,Para[temprole.id][0],Para[temprole.id][1]);
			if (temprole.id == 25) {// 闪电
				//g.drawImage(imgSd, tempx, tempy, TopLeft);
				g.drawRegion(imgProp, 40, 0, 40, 40, 0, tempx, tempy - 20, TopLeft);
				//System.out.println(tempy);
			} else if (temprole.id == 26) {// 五角星
				//g.drawImage(imgXiao, tempx, tempy, TopLeft);
				g.drawRegion(imgProp, 0, 0, 40, 40, 0, tempx, tempy - 20, TopLeft);
				//System.out.println(tempy);
			} else if (temprole.id == 27) {// 小药
				//g.drawImage(imgProp, tempx, tempy, TopLeft);
				g.drawRegion(imgProp, 240, 0, 40, 40, 0, tempx, tempy - 20, TopLeft);
				//System.out.println(tempy);
			} else if (temprole.id == 28) {// 大药
				g.drawRegion(imgProp, 200, 0, 40, 40, 0, tempx, tempy - 20, TopLeft);;
			} else if (temprole.id == 31) {// 咔
				g.drawImage(imgKa, tempx, tempy, TopLeft);
			} else if (temprole.id == 32) {// 骨头
				g.drawImage(imgGu, tempx - temprole.frame
						* Para[temprole.id][0], tempy, TopLeft);
			} else if (temprole.id == 29) {// 泡
				/*g.drawRegion(imgPao, 100*temprole.frame, 0, 100, 180, 0, tempx - temprole.frame	* Para[temprole.id][0]75, tempy289, TopLeft);
				if (--temprole.velocityTime < 0) {
					temprole.frame = (temprole.frame + 1) % 3;
					temprole.velocityTime = 5;
				}*/
				//g.drawImage(imgPao, tempx - temprole.frame
				//		* Para[temprole.id][0], tempy, TopLeft);
				
			} else if (temprole.id == 36){ //主界面的泡泡
				g.drawRegion(imgPao1, 0, 0, 55, 55, 0, tempx - temprole.frame
						* Para[temprole.id][0], tempy, 0);
				if (--temprole.velocityTime < 0) {
					temprole.frame = (temprole.frame == 0 ? 1 : 0);
					temprole.velocityTime = 5;
				}
			} else if (temprole.id == 37) {
				g.drawRegion(imgPao2, 0, 0, 30, 30, 0, tempx - temprole.frame
						* Para[temprole.id][0], tempy, 0);
				if (--temprole.velocityTime < 0) {
					temprole.frame = (temprole.frame == 0 ? 1 : 0);
					temprole.velocityTime = 5;
				}
			}
			else if (temprole.id == 34) {// 贝壳
				if (--temprole.velocityTime < 0) {
					temprole.frame = (temprole.frame == 0 ? 1 : 0);
					temprole.velocityTime = ran.nextInt() % 10 + 20;
				}
				g.drawImage(imgWalk, tempx - temprole.frame
						* Para[temprole.id][0], tempy, TopLeft);
			} else if (temprole.id == 33 && temprole.cahoot.frame == 1
					&& temprole.status == 1) {// 珍珠
				if (--temprole.velocityTime < 0) {
					temprole.frame = (temprole.frame == 0 ? 1 : 0);
					temprole.velocityTime = 5;
				}
				g.drawImage(imgPearl, tempx - temprole.frame
						* Para[temprole.id][0], tempy, TopLeft);
			}
		}
	}
	
    //*******************************************
	Image imgPrompt;
	private int pageup = 1;
	private int pagedown;
	private int paoFlag = 0; //游戏界面泡泡控制变量
	private int paoFlag1 = 0;

	private void drawInfo(SGraphics g) {
		g.setClip(0, 0, ScreenW, ScreenH);
		g.drawImage(imgInfo, 0, 0, TopLeft);
		g.drawImage(imgLevel, 0, ScreenH - imgLevel.getHeight(), TopLeft);

		if (imgPrompt == null) {
			try {
				imgPrompt = Image.createImage("/prompt.png");
			} catch (Exception e) {
				e.printStackTrace();
				while (true) ;
			}
		}
		if (imgPrompt != null) {
			g.drawImage(imgPrompt, getWidth() - imgPrompt.getWidth() - 70, /*getHeight() - imgPrompt.getHeight()*/0, 0);
		}

		drawNum(g, own.lifeNum, 46, 39);// 画生命数量
		drawNum(g, Para[own.id][2] / 2, 76, ScreenH - imgLevel.getHeight() + 20);// 画级别(体积)
		drawNum(g, own.money, 252 - 74 - 70, 8);// 画金钱数

		// g.setClip(14+1,ScreenH-4-3,own.experience*55/Para[own.id][3],2);
		// g.drawImage(imgLine,14+1,ScreenH-4-3-4,TopLeft);//画经验
		g.setColor(34, 221, 3);
		g.fillRect(4, ScreenH - imgLevel.getHeight() + 47, own.experience * 199
				/ Para[own.id][3], 7);

		// g.setClip(112,20,own.nonceLife*55/Para[own.id][5],2);
		// g.drawImage(imgLine,112,20,TopLeft);//画生命值
		g.setColor(255, 0, 0);
		g.fillRect(237 - 74 - 71, 44, own.nonceLife * 114 / Para[own.id][5], 5);
		// g.setClip(112,25,own.velocityNum*55/Para[own.id][6],2);
		// g.drawImage(imgLine,112,25-2,TopLeft);//画行动值

		coin.frame = (coin.frame == 0 ? 3 : coin.frame - 1);
		// g.setClip(coin.mapx,coin.mapy,Para[coin.id][0],Para[coin.id][1]);
		// g.drawImage(imgCoin,coin.mapx-coin.frame*Para[coin.id][0],coin.mapy,TopLeft);//画金币
		g.drawRegion(imgCoin, coin.frame * Para[coin.id][0], 0,
				Para[coin.id][0], Para[coin.id][1], 0, 222 - 74 - 70, 1, 0);
		//demo
		/*g.setColor(244, 252, 1);
		g.drawString("欧耶游戏DEMO", 300, ScreenH - 20, TopLeft);*/
	}

	private void cleanConfine() {
		for (int i = 0; i < Confine.length; ++i) {
			Confine[i] = 0; // 清空
		}
	}

	private boolean Collide(int towards) {
		switch (towards) {
		case 4: // 左
			if (own.mapx <= 0)
				return true;
			return false;
		case 6: // 右
			if (own.mapx >= (mapw - Para[own.id][0]))
				return true;
			return false;
		case 2: // 上
			if (own.mapy <= 0)
				return true;
			return false;
		case 8: // 下
			if (own.mapy >= (maph - Para[own.id][1]))
				return true;
			return false;
		}
		return false;
	}

	public void moveRole(int towards) {
		tempnum = 320 - 44;
		switch (towards) {
		case 4: // 往左--主角
			own.direction = 1;
			if (Collide(4) == true || own.velocityNum <= 0)
				return;
			
			towards = 15 + own.id * 5;//25 + own.id * 10     //own.velocityNum * 10 / Para[own.id][6] * Para[own.id][4]
					/// 10 + 3;// 随行动值减少的速度
			//System.out.println("towards:"+towards);
			if (screenx < towards || (own.mapx - screenx) > tempnum) {
				own.mapx -= towards;
			} else {
				moveStep(-towards, 0);
			}

			break;
		case 6: // 往右--主角
			own.direction = 0;
			
			if (Collide(6) == true || own.velocityNum <= 0)
				return;
			towards = 15 + own.id * 5;//25 + own.id * 10     //own.velocityNum * 10 / Para[own.id][6] * Para[own.id][4]
					/// 10 + 3;// 随行动值减少的速度
			//System.out.println("towards:"+towards);
			if ((screenx + ScreenW + towards) >= mapw
					|| (own.mapx - screenx) < tempnum) {
				own.mapx += towards;
			} else {
				moveStep(towards, 0);
			}
			break;
		case 2: // 往上--主角
			if (Collide(2) == true || own.velocityNum <= 0)
				return;
			towards = 15 + own.id * 5;//25 + own.id * 10     // own.velocityNum * 10 / Para[own.id][6] * Para[own.id][4]
					/// 10 + 3;// 随行动值减少的速度
			//System.out.println("towards:"+towards);
			if (screeny < towards || (own.mapy - screeny) > tempnum) {
				own.mapy -= towards;
			} else {
				moveStep(0, -towards);
			}
			break;
		case 5: // 往下--主角
			if (Collide(8) == true || own.velocityNum <= 0)
				return;
			towards = 15 + own.id * 10;//15 + own.id * 5     //own.velocityNum * 10 / Para[own.id][6] * Para[own.id][4]
					/// 10 + 3;// 随行动值减少的速度
			//System.out.println("towards:"+towards);
			if ((screeny + ScreenH + towards) >= maph
					|| (own.mapy - screeny) < tempnum) {
				own.mapy += towards;
			} else {
				moveStep(0, towards);
			}
			break;
		}
		if (System.currentTimeMillis() > clock1) {// 行动值消耗
			// own.velocityNum -= 2;
			clock1 = System.currentTimeMillis() + 50;
		}
	}

	private void moveStep(int stepx, int stepy) {
		own.mapx += stepx;
		own.mapy += stepy;
		screenx += stepx;
		screeny += stepy;
	}

	private void drawMap1(SGraphics g) {

		for (int i = 0; i < Gate[tollGate][2]; ++i) {
			g.setClip(0, 0, ScreenW, ScreenH);
			if (!((i * Gate[tollGate][0] >= screenx + ScreenW) || (i
					* Gate[tollGate][0] + Gate[tollGate][0] <= screenx))) {
				g.drawImage(imgMap, i * Gate[tollGate][0] - screenx,
						0 - screeny, TopLeft);
				g.drawRegion(imgPao, 100*paoFlag, 0, 100, 180, 0, /*tempx - temprole.frame	* Para[temprole.id][0]*/i * Gate[tollGate][0] - screenx + 75, /*tempy*/289, TopLeft);
				if (++paoFlag1 > 3) {
					paoFlag = (paoFlag + 1) % 3;
					paoFlag1 = 0;
				}
			}
		}
		
		/*
		 * int x, y; x = screenx%Gate[tollGate][0]; y = 0-screeny;
		 * g.drawRegion(imgMap, x, 0, Gate[tollGate][0]-x, ScreenH, 0, 0, 0 -
		 * screeny, TopLeft); g.drawRegion(imgMap, 0, 0, x, ScreenH, 0, x, 0 -
		 * screeny, TopLeft);
		 */
	}

	private void runDg() {
		--levin.velocityTime;
		if (levin.velocityTime < 0) {
			if (levin.velocityNum > 0) {
				--levin.velocityNum;
				levin.velocityTime = 3;
				levin.mapx = Math.abs(ran.nextInt()
						% (ScreenW - Para[levin.id][0]))
						+ screenx;
				levin.mapy = -Math
						.abs(ran.nextInt() % (Para[levin.id][1] >> 1))
						+ screeny;
			} else {
				while (npcs.size() > 0) {
					temprole = (Role) (npcs.elementAt(0));
					Confine[temprole.id] -= 1;
					own.money += Para[temprole.id][5];
					temprole.id = 31;// 变成咔
					temprole.velocityTime = 2;// 持续显示时间
					npcs.removeElementAt(0);
					props.addElement(temprole);
				}
				state = 8;// game
			}
		}
	}

	private void drawDg(SGraphics g) {
		drawMap1(g);
		drawNpc(g);
		drawProp(g);
		tempx = levin.mapx - screenx;
		tempy = levin.mapy - screeny;
		levin.frame = (levin.frame == 0 ? 1 : 0);
		g.setClip(tempx, tempy, Para[levin.id][0], Para[levin.id][1]);
		g.drawImage(imgDg, tempx - levin.frame * Para[levin.id][0], tempy,
				TopLeft);
		drawInfo(g);
	}

	private void runSelect(int i) {
		// selectIndex对应主角选择图片的索引,3,7,11
		/*
		 * if ( (key & Key04) != 0 || (key & Key16) != 0) { //左 selectIndex =
		 * (selectIndex == 3?3:selectIndex - 4); } else if ( (key & Key06) != 0
		 * || (key & Key17) != 0) { //右 selectIndex = (selectIndex ==
		 * 11?11:selectIndex + 4); }else if((key & Key05) != 0 || (key & Key18)
		 * != 0){ int[] res = {3,7,11}; for(int i = res.length - 1; i >= 0;
		 * --i){ if(res[i] != selectIndex){ imgRole[res[i]] = null; } }
		 */
		selectIndex = 3;
		imgSelect2 = null;
		cleanConfine();
		if (!npcs.isEmpty()) {
			npcs.removeAllElements();
		}
		if (!props.isEmpty()) {
			props.removeAllElements();
		}

		// TODO :创建珍珠
		/*
		 * int conchs[][] = { {78,184}, {263,180}, {353,161}, {417,172},
		 * {660,180}, {778,186} }; for(int i = 0; i < conchs.length; ++i){ Role
		 * temprole1,temprole2; temprole1 = new Role(); temprole1.id = 33; //珍珠
		 * temprole1.mapx = conchs[i][0] + 5; temprole1.mapy = conchs[i][1] + 4;
		 * temprole1.velocityTime = 5; //持续显示时间 temprole1.frame = 0;
		 * temprole1.status = 1;//代表有珍珠
		 * 
		 * temprole2 = new Role(); temprole2.id = 34;//贝壳 temprole2.mapx =
		 * conchs[i][0]; temprole2.mapy = conchs[i][1]; temprole2.velocityTime =
		 * 20;//持续显示时间 temprole2.frame = 0;
		 * 
		 * temprole1.cahoot = temprole2; temprole2.cahoot = temprole1;
		 * props.addElement(temprole1);//先珍珠后贝壳
		 * props.addElement(temprole2);//先珍珠后贝壳 }
		 */

		if (own == null) {
			own = new Role();
		}
		own.isOwn = true;
        own.scores = scores;
        System.out.println("own.scores: " + own.scores);
		own.money = money;
		own.id = id;
		own.status = 1;// 游动
		own.frame = 0;
		own.velocityNum = Para[own.id][6];
		own.experience = experience;
		own.nonceLife = nonceLife;
		own.lifeNum = lifeNum;

		own.mapx = 320 - 44;
		own.mapy = 350 - 80;
		// own.mapx = 64;
		// own.mapy = 96;
		eatNum = 0;
		screenx = screeny = 0;

		if (coin == null) {
			coin = new Role();
		}
		coin.id = 35;
		coin.frame = 0;
		coin.mapx = 113;// 屏幕绝对坐标
		coin.mapy = 2;// 屏幕绝对坐标

		load = loadVelocity = 0;
		state = 7;// load
		time = 50;// 加快加栽速度
		// }

	}

	private void runSelect() {

		selectIndex = 3;
		imgSelect2 = null;
		cleanConfine();
		if (!npcs.isEmpty()) {
			npcs.removeAllElements();
		}
		if (!props.isEmpty()) {
			props.removeAllElements();
		}

		if (own == null) {
			own = new Role();
		}
		own.isOwn = true;
		own.money = money;
		own.id = selectIndex - 3;
		own.status = 1;// 游动
		own.frame = 0;
		own.velocityNum = Para[own.id][6];
		own.experience = experience;
		own.scores = scores;
		own.nonceLife = Para[own.id][5];
		if(lifeNum == 0){
			own.lifeNum = 3;
		} else {
			own.lifeNum = lifeNum;
		}
		

		own.mapx = 320 - 44;
		own.mapy = 350 - 80;
		// own.mapx = 64;
		// own.mapy = 96;
		eatNum = 0;
		screenx = screeny = 0;

		if (coin == null) {
			coin = new Role();
		}
		coin.id = 35;
		coin.frame = 0;
		coin.mapx = 113;// 屏幕绝对坐标
		coin.mapy = 2;// 屏幕绝对坐标

		load = loadVelocity = 0;
		state = 7;// load
		time = 50;// 加快加栽速度
		// }
	}

	private void drawSelect(SGraphics g) throws Exception {
		/*
		 * if(imgSelect1 == null){ imgSelect1 =
		 * Image.createImage("/select1.png"); imgSelect2 =
		 * Image.createImage("/select2.png"); startx = 10; starty = 100; }
		 * switch(selectIndex){ case 11: if(imgRole[11] == null){ imgRole[11] =
		 * Image.createImage("/own_tortoise4.png"); } str = new String[2];
		 * str[0] = "肥油鱼："; str[1] = "出生在南极，顾名思义，一身厚重的脂肪使他的体力出类拔萃，恢复力更是惊人。";
		 * tempx = 131= Image.createImage("/own_penguin4.png"); } str = new
		 * String[2];; tempy = 36; break; case 7: if(imgRole[7] == null){
		 * imgRole[7]  str[0] = "胖胖鱼："; str[1] = "有张类似乌龟坚硬的壳，虽然速度慢但防御力强是它制胜的法宝。";
		 * tempx = 68; tempy = 36; break; case 3: if(imgRole[3] == null){
		 * imgRole[3] = Image.createIm"; str[1] =
		 * "活泼可爱的快乐鱼速度是它的强项age("/own_fish4.png"); } str = new
		 * String[2]; str[0] = "快乐鱼：，但由于身材娇小体力和防御也是它的缺点。"; tempx = 8; tempy = 36; break;
		 * } selectDir = (selectDir == 0?1:0); cleanScreen(g,0x0);
		 * g.drawImage(imgSelect1,2,30,TopLeft);
		 * g.setClip(selectIndex/4*58+2,30,56,56);
		 * g.drawImage(imgSelect2,2,30,TopLeft);//选中为高亮
		 * g.setClip(tempx,tempy,Para[selectIndex][0],Para[selectIndex][1]);
		 * g.drawImage
		 * (imgRole[selectIndex],tempx-3*Para[selectIndex][0],tempy-selectDir
		 * *Para[selectIndex][1],TopLeft);//选中
		 * drawStr(g,10,100,ScreenW-10,ScreenH-4);
		 */
	}

	private void runDead() {
		own.mapy -= 4;
		if (own.mapy < screeny) {
			own.status = 1;// 变为游动
			cleanConfine();
			npcs.removeAllElements();
			props.removeAllElements();
			if (own.lifeNum > 0) {
				own.nonceLife = Para[own.id][5];
				own.velocityNum = Para[own.id][6];
				own.lifeNum -= 1;
				isRed = false;// 将未画的标记去掉
				state = 15; // ready
			} else {
				// soundStop(0);
				timeClock = System.currentTimeMillis() + 2000;
				state = 12;// over
			}
		}
	}

	private void drawDead(SGraphics g){
		drawMap1(g);
		drawProp(g);
		drawNpc(g);
		drawInfo(g);
	}

	private void runReady() {
		own.mapy += 4;
		if (own.mapy > (screeny + ScreenH / 2)) {
			state = 8;// game
		}
	}

	private void drawReady(SGraphics g) {
		drawMap1(g);
		drawNpc(g);
		drawInfo(g);
	}

	private void runMenu() {
		if (keyState.containsAndRemove(KeyCode.UP)) {
			menuIndex = (menuIndex + 3 - 1) % 3;
			// menuIndex = (menuIndex == 0?0:menuIndex - 1);
		} else if (keyState.containsAndRemove(KeyCode.DOWN)) {
			// menuIndex = (menuIndex == 3?3:menuIndex + 1);
			menuIndex = (menuIndex + 1) % 3;
		} else if (keyState.containsAndRemove(KeyCode.OK)) {
			if (menuIndex == 0) {// 继续游戏
				imgMenu = null;
				System.gc();
				// if(soundState == true){
				// soundPlay(0,-1);
				// }
				state = 8;// game

			}/*
			 * else if(menuIndex == 1){//音效开关 state = 17;//sound }
			 */else if (menuIndex == 1) {// 帮助
				state = 19;// menu->help
			} else if (menuIndex == 2) {// 退出
				try{
					ServiceWrapper sw = getServiceWrapper();
					GameRecord gr = sw.readRecord(attainmentId);
					if(((gr==null && own.scores>0) || (gr!=null && gr.getScores()<=own.scores) && own.scores>0)){
						gameRecord.saveGameRecord(own);
					}
					GameAttainment ga = sw.readAttainment(attainmentId);
					if(((ga==null && own.scores>0) || (ga!=null && ga.getScores()<=own.scores) && own.scores>0)){
						gameRecord.saveGameAttainment(own);
					}
				} catch (Exception e){
					System.out.println("服务器连接失败!");
					e.printStackTrace();
					state = 3;
				}
				
				for (int i = props.size() - 1; i >= 0; --i) {
					props.removeElementAt(i);
				}
				imgCoin = imgKa = imgPao = imgGu = imgDg = imgLine = null;
				imgPearl = imgWalk = imgMap = imgInfo = imgLevel = imgNumber = null;
				imgPrompt = foot2 = imgProp = imgEye = imgFish = null;
				for (int i = imgRole.length - 1; i >= 0; --i) {
					imgRole[i] = null;
				}
				for (int i = wings.length - 1; i >= 0; --i) {
					wings[i] = null;
				}
				for (int i = eyes.length - 1; i >= 0; --i) {
					eyes[i] = null;
				}
				state = 3;// main
			}
		}
	}

	private void drawMenu(SGraphics g) throws Exception {
		if (imgMenu == null) {
			imgMenu = Image.createImage("/menu.png");
		}
		drawMap1(g);
		drawProp(g);
		drawNpc(g);
		drawInfo(g);
		tempx = (ScreenW - imgMenu.getWidth()) / 2;
		tempy = (ScreenH - imgMenu.getHeight()) / 2;
		g.setClip(0, 0, ScreenW, ScreenH);
		g.drawImage(imgMenu, tempx, tempy, TopLeft);
		int coinX, sx, sy;
		int menuW = 125, menuH = 44, coinW = 32, coinH = 32;
		coinX = coin.frame * coinW;
		sx = tempx - coinW - 1;
		sy = tempy + menuIndex * menuH + (menuH - coinH) / 2;
		g.drawRegion(imgCoin, coinX, 0, coinW, coinH, 0, sx, sy, 0);

		sx = tempx + menuW + 1;
		g.drawRegion(imgCoin, coinX, 0, coinW, coinH, 0, sx, sy, 0);
		/*
		 * g.setClip(tempx-20,tempy+menuIndex*19,Para[coin.id][0],Para[coin.id][1
		 * ]);
		 * g.drawImage(imgCoin,tempx-20-Para[coin.id][0],tempy+menuIndex*19,TopLeft
		 * );//左
		 * g.setClip(tempx+62+5,tempy+menuIndex*19,Para[coin.id][0],Para[coin
		 * .id][1]);
		 * g.drawImage(imgCoin,tempx+62+5-3*Para[coin.id][0],tempy+menuIndex
		 * *19,TopLeft);//右
		 */
	}
	
	
	private void runRanklist(){
		
		if (keyState.containsAndRemove(KeyCode.NUM0)){
			imgRank = null; imgPage = null;
			state = 3; // main
		}
		if(keyState.containsAndRemove(KeyCode.UP)){
		     page = 0;
		     pageup = 1;
		     pagedown = 0;
		}
		if(keyState.containsAndRemove(KeyCode.DOWN)){
			if(rankList == null || rankList.length <7){
				  page = 0;
				  pageup = 1;
				  pagedown = 0;
			} else {
				   page = 1;
				   pageup = 0;
				   pagedown = 1;
			}
		}
	
	}
	
	private void drawRanklist(SGraphics g)throws Exception{
	
		if (imgRank == null || imgPage == null ) {
			imgRank = Image.createImage("/rank.jpg");
			imgPage = Image.createImage("/pageup_down.png");
		}
		
		g.setClip(0, 0, ScreenW, ScreenH);
		g.drawImage(imgRank, 0, 0, TopLeft);
		g.drawRegion(imgPage, pageup * (imgPage.getWidth()/2), 0, imgPage.getWidth()/2, imgPage.getHeight()/2, 0, 576, 175, TopLeft);
		g.drawRegion(imgPage, pagedown * (imgPage.getWidth()/2), imgPage.getHeight()/2, imgPage.getWidth()/2, imgPage.getHeight()/2, 0, 576, 265, TopLeft);
		g.setColor(255,255,255);
		g.setFont(largeFont);
		int rankW = 110, rankH = 90 , cellW = 125, cellH = 56;
		int pageSize;
		if(rankList == null){
			pageSize = 0;
		} else {
			pageSize = rankList.length > 6 ? 6 : rankList.length;
		}
		if (page == 0) {
			for (int i = 0; i < pageSize; i++) {
				String r = String.valueOf(rankList[i].getRanking());
				String str = String.valueOf(rankList[i].getScores());
                
				g.drawString(r, 110, rankH + i * cellH, 0);
				g.drawString(rankList[i].getUserId(), 260, rankH + i * cellH, 0);
				g.drawString(str, 480, rankH + i * cellH, 0);
				if(rankList[i].getUserId().equals(getEngineService().getUserId())) {
					rank = rankList[i].getRanking();
				}
			}
		}
		if (page == 1) {
			if (rankList.length > 6) {
				for (int j = 6; j < rankList.length; j++) {
					String r = String.valueOf(rankList[j].getRanking());
					String str2 = String.valueOf(rankList[j].getScores());
					g.drawString(r, 110, rankH + (j - 6) * cellH, 0);
					g.drawString(rankList[j].getUserId(), 260, rankH + (j - 6) * cellH, 0);
					g.drawString(str2, 480, rankH + (j - 6) * cellH, 0);
					if(rankList[j].getUserId().equals(getEngineService().getUserId())) {
						rank = rankList[j].getRanking();
					}
				}
			}
		}
		String r2 = String.valueOf(rank);
		if(rank < 1 || rank > 10){
			r2 = "榜上无名";
		}
		g.drawString(r2, 370, 430, 0); //画当前用户的排名
	}

	private void runHelp(int tag) {
		if (keyState.containsAndRemove(KeyCode.NUM0)) {
			imgFrm = imgHelp = null;
			// needDrawMainBG = true;
			if (tag == 3) {
				state = 3;// main
			} else if (tag == 16) {
				state = 16;// menu
			}
		}
		if (keyState.containsAndRemove(KeyCode.OK)) {// 确定
			imgFrm = imgHelp = null;
			// needDrawMainBG = true;
			if (tag == 3) {
				state = 3;// main
			} else if (tag == 16) {
				state = 16;// menu
			}
		}
	}

	public void showHelp(SGraphics g) {
		if (imgHelp == null || imgHelp2 == null) {
			try {
				imgHelp = Image.createImage("/help.jpg");
				imgHelp2 = Image.createImage("/back.png");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		g.setClip(0, 0, getWidth(), getHeight());
		g.drawImage(imgHelp, 0, 0, 0);
		int help[][] = {{564, 454},{564, 454}};
		for (int i = 0; i < help.length; i++) {
			g.drawRegion(imgHelp2, (helpIndex != i) ? 0 : 70, 0, 70, 71, 0, help[i][0], help[i][1], 0);
		}
	}

	private void drawHelp(SGraphics g) throws Exception {
		if (imgHelp == null || imgFrm == null) {
			imgHelp = Image.createImage("/help.png");
			imgFrm = Image.createImage("/frm.jpg");
			// imgCom = Image.createImage("/command.png");
		}
		// cleanScreen(g,0x0);
		g.setClip(0, 0, ScreenW, ScreenH);
		g.drawImage(imgFrm, 0, 0, TopLeft);
		/*
		 * g.drawImage(imgHelp,10,10,TopLeft);
		 * g.setClip(ScreenW-12,ScreenH-12,12,12);
		 * g.drawImage(imgCom,ScreenW,ScreenH,BottomRight);
		 */
		int startX = 177, startY = 172, cellW = 25, cellH = 25, sy;
		int textY = (cellH - g.getFont().getHeight()) / 2;
		String[] helpText = { "闪电消灭屏幕上的所有其他鱼", "小药水恢复50点血", "苹果恢复100点血",
				"无敌药水10秒内无敌:按7键", "药丸解除疾病:按1键" };

		int[] helpImageID = { 7, 5, 4, /* 6, */3 };

		g.setColor(0);
		for (int i = 0; i < helpText.length; ++i) {
			sy = startY + i * (cellH + 8);
			g.drawRegion(imgHelp, helpImageID[i] * cellW, 0, cellW, cellH, 0,
					startX, sy, 0);
			sy = sy + textY;
			g.drawString(helpText[i], startX + cellW + 12, sy, 0);
		}

		g.drawString("请按0键返回", 177, 395, 0);
	}


	// sound**********************************************************
	/*
	 * private String filename[]={"/yuil.mid"}; private Player play[]=new
	 * Player[filename.length]; private boolean soundState = true;
	 * 
	 * private void soundInit(){ try{ for (int i = filename.length - 1; i >= 0;
	 * --i) { InputStream is =
	 * Class.forName("java.util.Random").getResourceAsStream(filename[i]);
	 * play[i] = Manager.createPlayer(is, "audio/midi"); play[i].prefetch(); }
	 * }catch(Exception e){ e.printStackTrace(); } }
	 * 
	 * private void soundPlay(int index,int count){ try{ if (index >=
	 * filename.length || play[index] == null)return; if (play[index].getState()
	 * == Player.STARTED) play[index].stop(); if (play[index].getState() ==
	 * Player.PREFETCHED) { play[index].setLoopCount(count);
	 * play[index].start(); } }catch(Exception e){ e.printStackTrace(); } }
	 * 
	 * private void soundStop(int index){ try{ if (index >= filename.length ||
	 * play[index] == null)return; play[index].stop(); }catch(Exception e){
	 * e.printStackTrace(); } }
	 */
	// ***************************************************************
}
