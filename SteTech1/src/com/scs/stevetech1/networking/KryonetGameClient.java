package com.scs.stevetech1.networking;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.server.Globals;
import com.sun.media.jfxmedia.logging.Logger;

import ssmith.lang.Functions;
import ssmith.lang.NumberFunctions;

public class KryonetGameClient implements IGameMessageClient {

	private IMessageClientListener listener;
	private Client client;

	public KryonetGameClient(String ip, int tcpPort, int udpPort, IMessageClientListener _listener, int timeout, Class<? extends Object>[] msgClasses) throws IOException {
		listener = _listener;

		Logger.setLevel(Logger.DEBUG);

		client = new Client(Globals.KRYO_WRITE_BUFFER_SIZE, Globals.KRYO_OBJECT_BUFFER_SIZE);
		KryonetGameServer.registerClasses(client.getKryo());
		if (msgClasses != null) {
			for(Class c : msgClasses) {
				client.getKryo().register(c);
			}
		}
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

		client.connect(timeout, ip, tcpPort, udpPort);
	}


	@Override
	public boolean isConnected() {
		return client.isConnected();
	}


	@Override
	public void sendMessageToServer(final MyAbstractMessage msg) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Sending to server: " + msg);
		}

		if (Globals.RELEASE_MODE) {// || Globals.MAX_ARTIFICIAL_COMMS_DELAY == 0) {

			if (msg.isTCP()) {
				client.sendTCP(msg);
			} else {
				client.sendUDP(msg);
			}
			
		} else {

			if (msg.isTCP()) {
				client.sendTCP(msg); // todo - delay
			} else {
				if (!KryonetGameServer.isPacketDropped()) {
					if (Globals.MAX_ARTIFICIAL_COMMS_DELAY <= 0) {
						client.sendUDP(msg);
					} else { 
						Thread t = new Thread("CommsDelayThread") {

							@Override
							public void run() {
								Functions.sleep(NumberFunctions.rnd(Globals.MIN_ARTIFICIAL_COMMS_DELAY, Globals.MAX_ARTIFICIAL_COMMS_DELAY));
								client.sendUDP(msg);
							}

						};
						t.start();
					}
				}
			}
		}
	}


	@Override
	public void close() {
		client.close();

	}

}
