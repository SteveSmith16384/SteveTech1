package com.scs.stetech1.networking;

import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.server.ClientData;

public interface IMessageServerListener {

	void connectionAdded(int id, Object net);
	
	void messageReceived(int clientid, MyAbstractMessage msg);
	
	void connectionRemoved(int id);
	
}
