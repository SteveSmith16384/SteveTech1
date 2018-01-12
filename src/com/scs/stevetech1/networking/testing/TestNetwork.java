package com.scs.stevetech1.networking.testing;

import java.io.IOException;

import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.TestMessage;
import com.scs.stevetech1.networking.IMessageClient;
import com.scs.stevetech1.networking.IMessageClientListener;
import com.scs.stevetech1.networking.IMessageServer;
import com.scs.stevetech1.networking.IMessageServerListener;
import com.scs.stevetech1.networking.KryonetClient;
import com.scs.stevetech1.networking.KryonetServer;
import com.scs.stevetech1.server.Globals;

public class TestNetwork implements IMessageServerListener, IMessageClientListener {
	
	private IMessageServer server;
	private IMessageClient client;

	public TestNetwork() throws IOException {
		server = new KryonetServer(Globals.PORT, Globals.PORT, this);
		//server.setListener(this);
		client = new KryonetClient(Globals.SERVER_IP_ADDRESS, Globals.PORT, Globals.PORT, this);
		
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
