package com.scs.stevetech1.lobby;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.networking.IMessageClientListener;
import com.scs.stevetech1.networking.KryonetGameServer;
import com.scs.stevetech1.server.Globals;
import com.sun.media.jfxmedia.logging.Logger;

import ssmith.lang.NumberFunctions;

public class KryonetLobbyClient {

	private IMessageClientListener listener;
	private Client client;

	public KryonetLobbyClient(String ip, int tcpPort, int udpPort, IMessageClientListener _listener, int timeout) throws IOException {
		listener = _listener;

		Logger.setLevel(Logger.DEBUG);

		client = new Client();
		KryonetLobbyServer.registerMessages(client.getKryo());
		client.setIdleThreshold(timeout);
		client.setTimeout(timeout);

		client.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if (Globals.DEBUG_MSGS) {
					Globals.p("Rcvd " + object);
				}
				if (object instanceof MyAbstractMessage) {
					MyAbstractMessage msg = (MyAbstractMessage)object;
					listener.messageReceived(msg);
				}
			}

			public void connected (Connection connection) {
				listener.connected();
			}

			public void disconnected (Connection connection) {
				listener.disconnected();
			}
			
			public void idle(Connection connection) {
				//Globals.p(this.getClass().getSimpleName() + " is Idle!");
			}
		});

		//client.start();  Not daemon
		Thread t = new Thread(client);
		t.setDaemon(true);
		t.start();

		client.connect(1000, ip, tcpPort, udpPort);
	}


	public void sendMessageToServer(final MyAbstractMessage msg) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Sending to server: " + msg);
		}

		// todo - this
		//if (Globals.RELEASE_MODE || Globals.MAX_ARTIFICIAL_COMMS_DELAY == 0) {
			if (msg.isReliable()) {
				client.sendTCP(msg);
			} else {
				client.sendUDP(msg);
			}
		/*}
		else {
			Thread t = new Thread("CommsDelayThread") {
				@Override
				public void run() {
					try {
						Thread.sleep(NumberFunctions.rnd(Globals.MIN_ARTIFICIAL_COMMS_DELAY, Globals.MAX_ARTIFICIAL_COMMS_DELAY));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (msg.isReliable()) {
						client.sendTCP(msg);
					} else {
						client.sendUDP(msg);
					}
				}
			};
			t.start();
		}*/

	}


	public void close() {
		client.close();

	}

}
