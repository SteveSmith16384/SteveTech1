package com.scs.stevetech1.networking;

import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.server.ClientData;

public interface IMessageServerListener {

	void connectionAdded(int id, Object net);
	
	void messageReceived(int clientid, MyAbstractMessage msg);
	
	void connectionRemoved(int id);
	
}
