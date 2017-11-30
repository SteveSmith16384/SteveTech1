package com.scs.stetech1.networking;

import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.server.ClientData;

public interface IMessageServer {

	//void setListener(IMessageServerListener _listener);
	
	int getNumClients();
	
	void sendMessageToAll(MyAbstractMessage msg);
	
	void sendMessageToClient(ClientData client, MyAbstractMessage msg);
	
	void close();
	
}
