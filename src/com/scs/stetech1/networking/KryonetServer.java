package com.scs.stetech1.networking;

import java.io.IOException;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.GameStatusMessage;
import com.scs.stetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stetech1.netmessages.GeneralCommandMessage;
import com.scs.stetech1.netmessages.GeneralCommandMessage.Command;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.netmessages.PlayerInputMessage;
import com.scs.stetech1.netmessages.PlayerLeftMessage;
import com.scs.stetech1.netmessages.RemoveEntityMessage;
import com.scs.stetech1.netmessages.TestMessage;
import com.scs.stetech1.netmessages.UnknownEntityMessage;
import com.scs.stetech1.netmessages.WelcomeClientMessage;
import com.scs.stetech1.server.ClientData;
import com.scs.stetech1.server.Settings;

public class KryonetServer implements IMessageServer {

	private IMessageServerListener listener;
	private Server server;

	public KryonetServer(IMessageServerListener _listener, int tcpport, int udpport) throws IOException {
		listener = _listener;

		server = new Server();
		registerMessages(server.getKryo());
		server.start();
		server.bind(tcpport, udpport);

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

			public void connected (Connection connection) {
				listener.connectionAdded(connection.getID(), connection);
			}

			public void disconnected (Connection connection) {
				listener.connectionRemoved(connection.getID());
			}
		});

	}


	public static void registerMessages(Kryo kryo) {
		// Classes used in messages
		kryo.register(Quaternion.class);
		kryo.register(Vector3f.class);
		kryo.register(HashMap.class);
		kryo.register(byte[].class);

		// Messages
		kryo.register(MyAbstractMessage.class);
		kryo.register(WelcomeClientMessage.class);
		kryo.register(PingMessage.class);
		kryo.register(NewPlayerRequestMessage.class);
		kryo.register(GameSuccessfullyJoinedMessage.class);
		kryo.register(PlayerInputMessage.class);
		kryo.register(UnknownEntityMessage.class);
		kryo.register(NewEntityMessage.class);
		kryo.register(EntityUpdateMessage.class);
		kryo.register(PlayerLeftMessage.class);
		kryo.register(RemoveEntityMessage.class);
		kryo.register(GeneralCommandMessage.class);
		kryo.register(Command.class);
		kryo.register(GameStatusMessage.class);
		kryo.register(TestMessage.class);

		// If you add any, don't forget to add the listener to the client or server!! 

	}


	@Override
	public int getNumClients() {
		return server.getConnections().length;
	}


	@Override
	public void sendMessageToAll(final MyAbstractMessage msg) {
		if (Settings.ARTIFICIAL_COMMS_DELAY == 0) {
			if (msg.isReliable()) {
				server.sendToAllTCP(msg);
			} else {
				server.sendToAllUDP(msg);
			}		
		}
		else {
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(Settings.ARTIFICIAL_COMMS_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (msg.isReliable()) {
						server.sendToAllTCP(msg);
					} else {
						server.sendToAllUDP(msg);
					}		
				}
			};
			t.start();
		}
	}


	@Override
	public void sendMessageToClient(final ClientData client, final MyAbstractMessage msg) {
		if (Settings.ARTIFICIAL_COMMS_DELAY == 0) {
			if (msg.isReliable()) {
				server.sendToTCP(client.id, msg);
			} else {
				server.sendToUDP(client.id, msg);
			}		
		}
		else {
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(Settings.ARTIFICIAL_COMMS_DELAY);
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

	@Override
	public void close() {
		server.close();

	}

}
