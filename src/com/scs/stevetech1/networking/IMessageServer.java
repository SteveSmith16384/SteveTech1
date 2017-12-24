package com.scs.stevetech1.networking;

import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.server.ClientData;

public interface IMessageServer {

	//void setListener(IMessageServerListener _listener);
	
	int getNumClients();
	
	void sendMessageToAll(MyAbstractMessage msg);
	
	void sendMessageToClient(ClientData client, MyAbstractMessage msg);
	
	void close();
	
}
