package com.scs.stetech1.lobby;

import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.networking.IMessageClientListener;
import com.scs.stetech1.networking.IMessageServer;
import com.scs.stetech1.networking.IMessageServerListener;
import com.scs.stetech1.networking.KryonetServer;
import com.scs.stetech1.server.Settings;

public class LobbyMain implements IMessageServerListener, IMessageClientListener {

	private IMessageServer networkServer;

	public static void main(String[] args) {
		new LobbyMain();

	}


	public LobbyMain() {
		networkServer = new KryonetServer(Settings.TCP_PORT, Settings.UDP_PORT, this);//_networkServer;
	}


	@Override
	public void connectionAdded(int id, Object net) {
		// Do nothing?
		
	}


	@Override
	public void messageReceived(int clientid, MyAbstractMessage msg) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void connectionRemoved(int id) {
		// Do nothing?
		
	}


	@Override
	public void connected() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void messageReceived(MyAbstractMessage msg) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void disconnected() {
		// TODO Auto-generated method stub
		
	}

}
