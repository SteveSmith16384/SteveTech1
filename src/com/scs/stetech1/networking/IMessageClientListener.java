package com.scs.stetech1.networking;

import com.scs.stetech1.netmessages.MyAbstractMessage;

public interface IMessageClientListener {

	void connected();
	
	void messageReceived(MyAbstractMessage msg);
	
	void disconnected();
	
}
