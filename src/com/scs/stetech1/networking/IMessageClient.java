package com.scs.stetech1.networking;

import com.scs.stetech1.netmessages.MyAbstractMessage;

public interface IMessageClient {

	boolean isConnected();
	
	void sendMessageToServer(MyAbstractMessage msg);
	
	void close();

}
