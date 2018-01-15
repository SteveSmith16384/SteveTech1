package com.scs.stevetech1.networking;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.GameStatusMessage;
import com.scs.stevetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.netmessages.PlayerLeftMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
import com.scs.stevetech1.netmessages.UnknownEntityMessage;
import com.scs.stevetech1.netmessages.WelcomeClientMessage;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

public class SpiderMonkeyServer implements IMessageServer, ConnectionListener, MessageListener<HostedConnection> {

	private Server myServer;
	private IMessageServerListener listener;
	private ExecutorService executor = Executors.newFixedThreadPool(20);

	public SpiderMonkeyServer() throws IOException {
		myServer = Network.createServer(Globals.GAME_PORT);

		registerMessages();

		myServer.start();
		myServer.addConnectionListener(this);

		myServer.addMessageListener(this, PingMessage.class);
		myServer.addMessageListener(this, NewPlayerRequestMessage.class);
		myServer.addMessageListener(this, GameSuccessfullyJoinedMessage.class);
		myServer.addMessageListener(this, PlayerInputMessage.class);
		myServer.addMessageListener(this, UnknownEntityMessage.class);
		myServer.addMessageListener(this, NewEntityMessage.class);
		myServer.addMessageListener(this, EntityUpdateMessage.class);
		myServer.addMessageListener(this, PlayerLeftMessage.class);

	}


	public static void registerMessages() {
		Serializer.registerClass(MyAbstractMessage.class);
		Serializer.registerClass(WelcomeClientMessage.class);
		Serializer.registerClass(PingMessage.class);
		Serializer.registerClass(NewPlayerRequestMessage.class);
		Serializer.registerClass(GameSuccessfullyJoinedMessage.class);
		Serializer.registerClass(PlayerInputMessage.class);
		Serializer.registerClass(UnknownEntityMessage.class);
		Serializer.registerClass(NewEntityMessage.class);
		Serializer.registerClass(EntityUpdateMessage.class);
		Serializer.registerClass(PlayerLeftMessage.class);
		Serializer.registerClass(RemoveEntityMessage.class);
		Serializer.registerClass(GeneralCommandMessage.class);
		Serializer.registerClass(GameStatusMessage.class);

		// If you add any, don't forget to add the listener to the client or server!! 

	}


	private void broadcast(final MyAbstractMessage msg) {
		if (Globals.ARTIFICIAL_COMMS_DELAY == 0) {
			myServer.broadcast(msg);
		}
		else {
			Thread t = new Thread("CommsDelayThread") {
				@Override
				public void run() {
					try {
						Thread.sleep(Globals.ARTIFICIAL_COMMS_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					myServer.broadcast(msg);
				}
			};
			t.start();
		}
	}


	private void broadcast(final HostedConnection conn, final MyAbstractMessage msg) {
		if (Globals.ARTIFICIAL_COMMS_DELAY == 0) {
			myServer.broadcast(Filters.equalTo(conn), msg);
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
					myServer.broadcast(Filters.equalTo(conn), msg);
				}
			};
			executor.execute(t);

		}
	}


	@Override
	public void messageReceived(HostedConnection source, Message m) {
		this.listener.messageReceived(source.getId(), (MyAbstractMessage)m);
		
	}


	@Override
	public void connectionAdded(Server server, HostedConnection conn) {
		this.listener.connectionAdded(conn.getId(), conn);
		
	}


	@Override
	public void connectionRemoved(Server server, HostedConnection conn) {
		this.listener.connectionRemoved(conn.getId());
		
	}


	@Override
	public int getNumClients() {
		return this.myServer.getConnections().size();
	}


	@Override
	public void sendMessageToAll(MyAbstractMessage msg) {
		this.broadcast(msg);
		
	}


	@Override
	public void sendMessageToClient(ClientData client, MyAbstractMessage msg) {
		this.broadcast((HostedConnection)client.networkObj, msg);
		
	}


	@Override
	public void close() {
		this.myServer.close();
		
	}


	/*@Override
	public void setListener(IMessageServerListener _listener) {
		//myServer.addMessageListener(_listener, classes);
		
	}

*/
}
