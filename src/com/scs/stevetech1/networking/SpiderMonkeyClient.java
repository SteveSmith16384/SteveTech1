package com.scs.stevetech1.networking;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.ErrorListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
import com.scs.stevetech1.netmessages.WelcomeClientMessage;
import com.scs.stevetech1.server.Globals;

public class SpiderMonkeyClient implements IMessageClient, ClientStateListener, ErrorListener<Object>, MessageListener<Client>  {

	private Client myClient;
	private IMessageClientListener listener;
	private ExecutorService executor = Executors.newFixedThreadPool(20);
	
	public SpiderMonkeyClient(IMessageClientListener _listener) throws IOException {
		listener = _listener;
		
		SpiderMonkeyServer.registerMessages();

		myClient = Network.connectToServer("localhost", Globals.PORT);
		myClient.addClientStateListener(this);
		myClient.addErrorListener(this);

		myClient.addMessageListener(this, PingMessage.class);
		myClient.addMessageListener(this, WelcomeClientMessage.class);
		myClient.addMessageListener(this, NewEntityMessage.class);
		myClient.addMessageListener(this, EntityUpdateMessage.class);
		myClient.addMessageListener(this, NewPlayerRequestMessage.class);
		myClient.addMessageListener(this, GameSuccessfullyJoinedMessage.class);
		myClient.addMessageListener(this, RemoveEntityMessage.class);
		myClient.addMessageListener(this, GeneralCommandMessage.class);

		myClient.start();

		//send(new NewPlayerRequestMessage("Mark Gray", 1));
	}


	@Override
	public void clientConnected(Client arg0) {
		Globals.p("Connected!");

	}


	@Override
	public void clientDisconnected(Client arg0, DisconnectInfo arg1) {
		Globals.p("clientDisconnected()");

	}


	@Override
	public void handleError(Object obj, Throwable ex) {
		Globals.p("Network error with " + obj + ": " + ex);
		ex.printStackTrace();
	}


	private void send(final Message msg) {
		if (Globals.ARTIFICIAL_COMMS_DELAY == 0) {
			myClient.send(msg);
		}
		else {
			Runnable t = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(Globals.ARTIFICIAL_COMMS_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					myClient.send(msg);
				}
			};
			executor.execute(t);

		}
	}


	@Override
	public void messageReceived(Client source, Message m) {
		listener.messageReceived((MyAbstractMessage)m);
		
	}


	@Override
	public void sendMessageToServer(MyAbstractMessage msg) {
		this.send(msg);
		
	}


	@Override
	public void close() {
		executor.shutdown();
		
	}


	@Override
	public boolean isConnected() {
		return myClient.isConnected();
	}



}
