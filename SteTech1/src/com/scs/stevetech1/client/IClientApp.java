package com.scs.stevetech1.client;

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
}
