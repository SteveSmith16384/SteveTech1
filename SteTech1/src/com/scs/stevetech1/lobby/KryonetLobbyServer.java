package com.scs.stevetech1.lobby;

import java.io.IOException;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.lobby.ListOfGameServersMessage;
import com.scs.stevetech1.netmessages.lobby.RequestListOfGameServersMessage;
import com.scs.stevetech1.netmessages.lobby.UpdateLobbyMessage;
import com.scs.stevetech1.networking.IMessageServerListener;
import com.scs.stevetech1.networking.KryonetGameServer;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;

public class KryonetLobbyServer {

	private IMessageServerListener listener;
	private Server server;
	private int timeoutMillis;

	public KryonetLobbyServer(int tcpport, int udpport, IMessageServerListener _listener, int _timeoutMillis) throws IOException {
		super();
		
		timeoutMillis = _timeoutMillis;
		server = new Server();
		registerMessages(server.getKryo());
		setListener(_listener);
		server.bind(tcpport, udpport);
		
		Thread t = new Thread(server);
		t.setDaemon(true);
		t.start();
		// server.start(); Not daemon!

	}


	public void setListener(IMessageServerListener _listener) {
		listener = _listener;
		server.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if (Globals.DEBUG_MSGS) {
					Globals.p("Rcvd " + object);
				}
				if (object instanceof MyAbstractMessage) {
					MyAbstractMessage msg = (MyAbstractMessage)object;
					listener.messageReceived(connection.getID(), msg);
				}
			}

			public void connected (Connection connection) {
				connection.setIdleThreshold(timeoutMillis);
				connection.setTimeout(timeoutMillis);

				listener.connectionAdded(connection.getID(), connection);
			}

			public void disconnected (Connection connection) {
				listener.connectionRemoved(connection.getID());
			}

			public void idle(Connection connection) {
				//Globals.p(this.getClass().getSimpleName() + " is Idle!");
			}
		});

	}

	public static void registerMessages(Kryo kryo) {	
		kryo.register(UpdateLobbyMessage.class);
		kryo.register(RequestListOfGameServersMessage.class);
		kryo.register(ListOfGameServersMessage.class);

		kryo.register(ArrayList.class);
}


	public void sendMessageToClient(final Connection client, final MyAbstractMessage msg) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Sending to client: " + msg);
		}
		// todo - this
		//if (Globals.RELEASE_MODE || Globals.MAX_ARTIFICIAL_COMMS_DELAY == 0) {
			if (msg.isTCP()) {
				server.sendToTCP(client.getID(), msg);
			} else {
				server.sendToUDP(client.getID(), msg);
			}		
		/*} else {
			Thread t = new Thread("CommsDelayThread") {
				@Override
				public void run() {
					try {
						Thread.sleep(NumberFunctions.rnd(Globals.MIN_ARTIFICIAL_COMMS_DELAY, Globals.MAX_ARTIFICIAL_COMMS_DELAY));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (msg.isReliable()) {
						server.sendToTCP(client.getID(), msg);
					} else {
						server.sendToUDP(client.getID(), msg);
					}		
				}
			};
			t.start();
		}*/

	}

}

