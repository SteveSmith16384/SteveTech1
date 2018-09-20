package com.scs.stevetech1.client;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

/**
 * This class is primarily to make unit testing easier.
 * @author stephencs
 *
 */
public interface IClientApp {

	int getPlayerID();
	
	long getRenderTime();
	
	long getServerTime();
	
	IEntity getEntity(int id);
	
	void sendMessage(MyAbstractMessage msg);
	
	SimpleGameData getGameData();
	
	void playSound(int soundId, int entityId, Vector3f _pos, float _volume, boolean _stream);
}
