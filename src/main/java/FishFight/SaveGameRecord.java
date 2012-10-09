package FishFight;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cn.ohyeah.itvgame.model.GameAttainment;
import cn.ohyeah.itvgame.model.GameRecord;
import cn.ohyeah.stb.game.ServiceWrapper;

public class SaveGameRecord{
	private FiCanvas engine;
	public SaveGameRecord(FiCanvas engine) {
		this.engine = engine;
	}

	/*存游戏成就,用于排名--返回0成功*/
	public int saveGameAttainment(Role own){
		ServiceWrapper sw = engine.getServiceWrapper();
		GameAttainment attainment = new GameAttainment();
		attainment.setAttainmentId(FiCanvas.attainmentId);
		attainment.setScores(own.scores);
		sw.saveAttainment(attainment);
			
		return sw.getServiceResult();
	}
	
	/*存游戏记录*/
	public int saveGameRecord(Role own){
		byte record[];
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);
		ServiceWrapper sw = engine.getServiceWrapper();
		try {
			dout.writeByte(FiCanvas.tollGate);
			dout.writeInt(own.experience);
			dout.writeInt(own.lifeNum);
			dout.writeInt(own.nonceLife);
			dout.writeInt(own.money);
			dout.writeInt(own.id);

			System.out.println("tollGate: " + FiCanvas.tollGate);
			System.out.println("experience: " + own.experience);
			System.out.println("lifeNum: " + own.lifeNum);
			System.out.println("nonceLife: " + own.nonceLife);
			System.out.println("money: " + own.money);
			System.out.println("id:" + own.id);
			record = bout.toByteArray();

			// rs.addRecord(record, 0, record.length);
			GameRecord gamerecord = new GameRecord();
			//gamerecord = new GameAttainment();
			gamerecord.setData(record);
			gamerecord.setRecordId(FiCanvas.attainmentId);
			gamerecord.setScores(own.scores);
			gamerecord.setRemark("remark 测试");

			sw.saveRecord(gamerecord);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				dout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				try {
					bout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		return sw.getServiceResult();
	}
	
	/*读档--返回0成功*/
	public int loadGameRecord(){
		ServiceWrapper sw = engine.getServiceWrapper();
		GameRecord gameRecord = sw.readRecord(FiCanvas.attainmentId);
		if(!sw.isServiceSuccessful() || gameRecord==null){
			return -1;
		}
		ByteArrayInputStream bin = new ByteArrayInputStream(gameRecord.getData());
		DataInputStream din = new DataInputStream(bin);
		try {
			FiCanvas.tollGate2 = din.readByte();
			FiCanvas.experience = din.readInt();
			FiCanvas.lifeNum = din.readInt();
			FiCanvas.nonceLife = din.readInt();
			FiCanvas.money = din.readInt();
			FiCanvas.id = din.readInt();
			FiCanvas.scores = gameRecord.getScores();

			System.out.println("tollGate: " + FiCanvas.tollGate2);
			System.out.println("experience: " + FiCanvas.experience);
			System.out.println("lifeNum: " + FiCanvas.lifeNum);
			System.out.println("nonceLife: " + FiCanvas.nonceLife);
			System.out.println("money: " + FiCanvas.money);
			System.out.println("id:"+ FiCanvas.id);

			System.out.println("read record");
		} catch (Exception e) {
			System.out.println("没有游戏记录,开始新的游戏!");
			return -1;
			//e.printStackTrace();
		}finally{
			try {
				din.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				try {
					bin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		return sw.getServiceResult();
	}
	
}
