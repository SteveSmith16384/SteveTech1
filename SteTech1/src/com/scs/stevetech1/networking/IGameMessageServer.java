package com.scs.stevetech1.networking;

import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.server.ClientData;

public interface IGameMessageServer {

	int getNumClients();
	
	//void sendMessageToAll(List<ClientData> clients, MyAbstractMessage msg);
	
	void sendMessageToClient(ClientData client, MyAbstractMessage msg);
	
	//void sendMessageToAllExcept(List<ClientData> clients, ClientData client, MyAbstractMessage msg);
	
	void close();
	
}
