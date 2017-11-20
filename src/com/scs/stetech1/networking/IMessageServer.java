package com.scs.stetech1.networking;

public interface IMessageServer {

	boolean AreAnyClientsConnectd();
	
	void clientRemoved();
}
