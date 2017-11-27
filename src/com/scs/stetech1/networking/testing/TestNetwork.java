package com.scs.stetech1.networking.testing;

import java.io.IOException;

import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.netmessages.TestMessage;
import com.scs.stetech1.networking.IMessageClient;
import com.scs.stetech1.networking.IMessageClientListener;
import com.scs.stetech1.networking.IMessageServer;
import com.scs.stetech1.networking.IMessageServerListener;
import com.scs.stetech1.networking.KryonetClient;
import com.scs.stetech1.networking.KryonetServer;
import com.scs.stetech1.server.Settings;

public class TestNetwork implements IMessageServerListener, IMessageClientListener {
	
	private IMessageServer server;
	private IMessageClient client;

	public TestNetwork() throws IOException {
		server = new KryonetServer(Settings.TCP_PORT, Settings.UDP_PORT);
		server.setListener(this);
		client = new KryonetClient(this);
		
	}

	public static void main(String[] args) {
		try {
			Settings.p("Started");
			new TestNetwork();
			Settings.p("Finished");
			
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
		Settings.p("Server rcvd id " + tmsg.num);
		if (tmsg.num < 10) {
			server.sendMessageToAll(new TestMessage(tmsg.num+1));
			Settings.p("Server sent " + (tmsg.num+1));
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
		Settings.p("Client rcvd id " + tmsg.num);
		if (tmsg.num < 10) {
			client.sendMessageToServer(new TestMessage(tmsg.num+1));
			Settings.p("Client sent " + (tmsg.num+1));
		}
		
	}

	@Override
	public void disconnected() {
		
	}

}
