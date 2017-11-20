package com.scs.stetech1.networking;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.server.Settings;

public class KryonetClient implements IMessageClient {

	private IMessageClientListener listener;
	private Client client;

	public KryonetClient(IMessageClientListener _listener) throws IOException {
		listener = _listener;

		client = new Client();
		client.connect(1000, Settings.IP_ADDRESS, Settings.TCP_PORT, Settings.UDP_PORT);

		// todo - register messages
	}

	@Override
	public boolean isConnected() {
		return client.isConnected();
	}

	@Override
	public void sendMessageToServer(MyAbstractMessage msg) {
		if (msg.isReliable()) {
			client.sendTCP(msg);
		} else {
			client.sendUDP(msg);
		}

	}

	@Override
	public void close() {
		client.close();

	}

}
