package com.scs.stetech1.networking;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.server.Settings;

public class KryonetClient implements IMessageClient {

	private IMessageClientListener listener;
	private Client client;

	public KryonetClient(IMessageClientListener _listener) throws IOException {
		listener = _listener;

		client = new Client();
		KryonetServer.registerMessages(client.getKryo());
		client.setIdleThreshold(0); // todo
		client.start();
		client.connect(1000, Settings.IP_ADDRESS, Settings.TCP_PORT, Settings.UDP_PORT);

		client.addListener(new Listener() {
			public void received (Connection connection, Object object) {
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
				Settings.p("Idle!"); // todo
			}
		});

	}


	@Override
	public boolean isConnected() {
		return client.isConnected();
	}


	@Override
	public void sendMessageToServer(final MyAbstractMessage msg) {
		if (Settings.ARTIFICIAL_COMMS_DELAY == 0) {
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
						Thread.sleep(Settings.ARTIFICIAL_COMMS_DELAY);
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
