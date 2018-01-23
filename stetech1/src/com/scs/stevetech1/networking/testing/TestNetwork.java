package com.scs.stevetech1.networking.testing;

import java.io.IOException;

import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.TestMessage;
import com.scs.stevetech1.networking.IGameMessageClient;
import com.scs.stevetech1.networking.IMessageClientListener;
import com.scs.stevetech1.networking.IGameMessageServer;
import com.scs.stevetech1.networking.IMessageServerListener;
import com.scs.stevetech1.networking.KryonetGameClient;
import com.scs.stevetech1.networking.KryonetGameServer;
import com.scs.stevetech1.server.Globals;

public class TestNetwork implements IMessageServerListener, IMessageClientListener {
	
	private static final int GAME_PORT = 1234;
	private static final String GAME_IP_ADDRESS = "localhost";
	
	private IGameMessageServer server;
	private IGameMessageClient client;

	public TestNetwork() throws IOException {
		server = new KryonetGameServer(GAME_PORT, GAME_PORT, this);
		//server.setListener(this);
		client = new KryonetGameClient(GAME_IP_ADDRESS, GAME_PORT, GAME_PORT, this);
		
	}

	public static void main(String[] args) {
		try {
			Globals.p("Started");
			new TestNetwork();
			Globals.p("Finished");
			
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	// Server

	@Override
	public void connectionAdded(int id, Object net) {
		server.sendMessageToAll(new TestMessage(1));
		server.sendMessageToAll(new TestMessage(1));
		server.sendMessageToAll(new TestMessage(1));
		server.sendMessageToAll(new TestMessage(1));
		server.sendMessageToAll(new TestMessage(1));
		
	}

	@Override
	public void messageReceived(int clientid, MyAbstractMessage msg) {
		TestMessage tmsg = (TestMessage)msg;
		Globals.p("Server rcvd id " + tmsg.num);
		if (tmsg.num < 10) {
			server.sendMessageToAll(new TestMessage(tmsg.num+1));
			Globals.p("Server sent " + (tmsg.num+1));
		} else {
			server.close();
			System.exit(0);
		}
		
		
	}

	@Override
	public void connectionRemoved(int id) {
		
	}

	
	// Client 
	
	@Override
	public void connected() {
		
	}
	

	@Override
	public void messageReceived(MyAbstractMessage msg) {
		TestMessage tmsg = (TestMessage)msg;
		Globals.p("Client rcvd id " + tmsg.num);
		if (tmsg.num < 10) {
			client.sendMessageToServer(new TestMessage(tmsg.num+1));
			Globals.p("Client sent " + (tmsg.num+1));
		}
		
	}

	@Override
	public void disconnected() {
		
	}

}
