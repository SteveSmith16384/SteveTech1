package com.scs.stetech1.networking;

import com.scs.stetech1.netmessages.MyAbstractMessage;

public interface IMessageClientListener {

	void messageReceived(MyAbstractMessage msg);
	
}
