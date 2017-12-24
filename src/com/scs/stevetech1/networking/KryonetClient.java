package com.scs.stevetech1.networking;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.server.Globals;
import com.sun.media.jfxmedia.logging.Logger;

public class KryonetClient implements IMessageClient {

	private IMessageClientListener listener;
	private Client client;

	public KryonetClient(IMessageClientListener _listener) throws IOException {
		listener = _listener;

		Logger.setLevel(Logger.DEBUG); // todo?

		client = new Client();
		KryonetServer.registerMessages(client.getKryo());
		client.setIdleThreshold(0); // todo
		client.setTimeout(0); // todo

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
				Globals.p("Idle!");
			}
		});

		client.start();
		client.connect(1000, Globals.IP_ADDRESS, Globals.TCP_PORT, Globals.UDP_PORT);
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

		if (Globals.ARTIFICIAL_COMMS_DELAY == 0) {
			if (msg.isReliable()) {
				client.sendTCP(msg);
			} else {
				client.sendUDP(msg);
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
						client.sendTCP(msg);
					} else {
						client.sendUDP(msg);
					}
				}
			};
			t.start();
		}

	}


	@Override
	public void close() {
		client.close();

	}

}
