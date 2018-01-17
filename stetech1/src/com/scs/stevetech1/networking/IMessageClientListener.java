package com.scs.stevetech1.networking;

import com.scs.stevetech1.netmessages.MyAbstractMessage;

public interface IMessageClientListener {

	void connected();
	
	void messageReceived(MyAbstractMessage msg);
	
	void disconnected();
	
}
