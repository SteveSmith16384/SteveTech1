package com.scs.stevetech1.networking;

import java.io.IOException;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoNetException;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityLaunchedMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage.UpdateData;
import com.scs.stevetech1.netmessages.GameOverMessage;
import com.scs.stevetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage.Command;
import com.scs.stevetech1.netmessages.ModelBoundsMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.PlaySoundMessage;
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.netmessages.PlayerLeftMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
import com.scs.stevetech1.netmessages.SimpleGameDataMessage;
import com.scs.stevetech1.netmessages.TestMessage;
import com.scs.stevetech1.netmessages.UnknownEntityMessage;
import com.scs.stevetech1.netmessages.WelcomeClientMessage;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.systems.client.LaunchData;

import ssmith.lang.NumberFunctions;

public class KryonetGameServer implements IGameMessageServer {

	private IMessageServerListener listener;
	private Server server;
	private int timeout;
	
	public KryonetGameServer(int tcpport, int udpport, IMessageServerListener _listener, int _timeout) throws IOException {
		super();

		server = new Server();
		timeout = _timeout;
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
				connection.setIdleThreshold(timeout);
				connection.setTimeout(timeout);

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
		// Classes used in messages
		kryo.register(Quaternion.class);
		kryo.register(Vector3f.class);
		kryo.register(HashMap.class);
		kryo.register(byte[].class);
		kryo.register(java.util.LinkedList.class);
		kryo.register(java.util.ArrayList.class);
		kryo.register(com.jme3.bounding.BoundingBox.class);
		kryo.register(com.jme3.bounding.BoundingSphere.class);
		
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
		kryo.register(UpdateData.class);
		kryo.register(PlayerLeftMessage.class);
		kryo.register(RemoveEntityMessage.class);
		kryo.register(GeneralCommandMessage.class);
		kryo.register(Command.class);
		kryo.register(SimpleGameDataMessage.class);
		kryo.register(TestMessage.class);
		kryo.register(AbilityUpdateMessage.class);
		kryo.register(SimplePlayerData.class);
		kryo.register(SimpleGameData.class);
		kryo.register(AvatarStartedMessage.class);
		kryo.register(EntityKilledMessage.class);
		kryo.register(EntityLaunchedMessage.class);
		kryo.register(LaunchData.class);
		kryo.register(AvatarStatusMessage.class);
		kryo.register(GameOverMessage.class);
		kryo.register(PlaySoundMessage.class);
		kryo.register(ModelBoundsMessage.class);
	}


	@Override
	public int getNumClients() {
		return server.getConnections().length;
	}


	public static boolean isPacketDropped() {
		return Globals.SIMULATE_DROPPED_PACKETS && NumberFunctions.rnd(0, 100) < Globals.PCENT_DROPPED_PACKETS;
	}

	
	@Override
	public void sendMessageToAll(final MyAbstractMessage msg) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Sending to all " + msg);
		}

		if (Globals.MAX_ARTIFICIAL_COMMS_DELAY == 0) {
			if (msg.isReliable()) {
				server.sendToAllTCP(msg);
			} else {
				if (!this.isPacketDropped()) {
					server.sendToAllUDP(msg);
				}
			}		
		}
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
						server.sendToAllTCP(msg);
					} else {
						if (!isPacketDropped()) {
							server.sendToAllUDP(msg);
						}
					}		
				}
			};
			t.start();
		}
	}


	@Override
	public void sendMessageToClient(final ClientData client, final MyAbstractMessage msg) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Sending to client: " + msg);
		}
		try {
			if (Globals.MAX_ARTIFICIAL_COMMS_DELAY == 0) {
				if (msg.isReliable()) {
					server.sendToTCP(client.id, msg);
				} else {
					if (!isPacketDropped()) {
						server.sendToUDP(client.id, msg);
					}
				}		
			}
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
							server.sendToTCP(client.id, msg);
						} else {
							if (!isPacketDropped()) {
								server.sendToUDP(client.id, msg);
							}
						}		
					}
				};
				t.start();
			}
		} catch (KryoNetException ex) {
			Globals.pe("Error sending to client: " + ex.getMessage());
		}

	}

	@Override
	public void close() {
		server.close();

	}

}
