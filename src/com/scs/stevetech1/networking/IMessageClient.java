package com.scs.stevetech1.networking;

import com.scs.stevetech1.netmessages.MyAbstractMessage;

public interface IMessageClient {

	boolean isConnected();
	
	void sendMessageToServer(MyAbstractMessage msg);
	
	void close();

}
