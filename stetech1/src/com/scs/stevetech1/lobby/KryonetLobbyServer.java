package com.scs.stevetech1.lobby;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.networking.IMessageServerListener;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

public class KryonetLobbyServer {

	private IMessageServerListener listener;
	private Server server;

	public KryonetLobbyServer(int tcpport, int udpport, IMessageServerListener _listener) throws IOException {
		server = new Server();
		registerMessages(server.getKryo());
		setListener(_listener);
		server.bind(tcpport, udpport);
		server.start();

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
				connection.setIdleThreshold(0); // todo
				connection.setTimeout(0); // todo

				listener.connectionAdded(connection.getID(), connection);
			}

			public void disconnected (Connection connection) {
				listener.connectionRemoved(connection.getID());
			}

			public void idle(Connection connection) {
				Globals.p("Idle!");
			}
		});

	}

	public static void registerMessages(Kryo kryo) {
		//kryo.register(RequestNewBulletMessage.class);
	}

/*
	@Override
	public int getNumClients() {
		return server.getConnections().length;
	}
*/

	public void sendMessageToClient(final ClientData client, final MyAbstractMessage msg) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Sending to client: " + msg);
		}
 		if (Globals.ARTIFICIAL_COMMS_DELAY == 0) {
			if (msg.isReliable()) {
				server.sendToTCP(client.id, msg);
			} else {
				server.sendToUDP(client.id, msg);
			}		
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
					if (msg.isReliable()) {
						server.sendToTCP(client.id, msg);
					} else {
						server.sendToUDP(client.id, msg);
					}		
				}
			};
			t.start();
		}

	}
/*
	@Override
	public void close() {
		server.close();

	}
*/
}

