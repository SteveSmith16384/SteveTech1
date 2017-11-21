package com.scs.stetech1.networking;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.server.ClientData;
import com.scs.stetech1.server.Settings;

public class KryonetServer implements IMessageServer {

	private IMessageServerListener listener;
	private Server server;

	public KryonetServer(IMessageServerListener _listener) throws IOException {
		listener = _listener;

		server = new Server();
		server.bind(Settings.TCP_PORT, Settings.UDP_PORT);

		server.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if (object instanceof MyAbstractMessage) {
					MyAbstractMessage msg = (MyAbstractMessage)object;
					listener.messageReceived(connection.getID(), msg);
					//System.out.println(request.text);

					//SomeResponse response = new SomeResponse();
					//response.text = "Thanks";
					//connection.sendTCP(response);
				}
			}
		});

	}

	@Override
	public int getNumClients() {
		return server.getConnections().length;
	}

	@Override
	public void sendMessageToAll(MyAbstractMessage msg) {
		if (msg.isReliable()) {
			server.sendToAllTCP(msg);
		} else {
			server.sendToAllUDP(msg);
		}		
	}

	
	@Override
	public void sendMessageToClient(ClientData client, MyAbstractMessage msg) {
		if (msg.isReliable()) {
			server.sendToTCP(client.id, msg);
		} else {
			server.sendToUDP(client.id, msg);
		}		

	}

	@Override
	public void close() {
		server.close();

	}

}
