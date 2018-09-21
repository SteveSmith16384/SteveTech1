package com.scs.stevetech1.networking;

import java.io.IOException;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.netmessages.AbilityActivatedMessage;
import com.scs.stevetech1.netmessages.AbilityReloadingMessage;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.ClientReloadRequestMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityUpdateData;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.GameLogMessage;
import com.scs.stevetech1.netmessages.GameOverMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage.Command;
import com.scs.stevetech1.netmessages.ModelBoundsMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.NumEntitiesMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.PlaySoundMessage;
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.netmessages.PlayerLeftMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
import com.scs.stevetech1.netmessages.SetAvatarMessage;
import com.scs.stevetech1.netmessages.ShowMessageMessage;
import com.scs.stevetech1.netmessages.SimpleGameDataMessage;
import com.scs.stevetech1.netmessages.TestMessage;
import com.scs.stevetech1.netmessages.connecting.GameSuccessfullyJoinedMessage;
import com.scs.stevetech1.netmessages.connecting.HelloMessage;
import com.scs.stevetech1.netmessages.connecting.JoinGameFailedMessage;
import com.scs.stevetech1.netmessages.connecting.JoinGameRequestMessage;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.Functions;
import ssmith.lang.NumberFunctions;

public class KryonetGameServer implements IGameMessageServer {

	private Server server;
	private int timeout;

	public KryonetGameServer(int tcpport, int udpport, IMessageServerListener _listener, int _timeout, Class<? extends MyAbstractMessage>[] msgClasses) throws IOException {
		super();

		server = new Server(Globals.KRYO_WRITE_BUFFER_SIZE, Globals.KRYO_OBJECT_BUFFER_SIZE);
		timeout = _timeout;

		//Log.set(Log.LEVEL_DEBUG);

		registerMessages(server.getKryo());
		if (msgClasses != null) {
			for(Class<? extends MyAbstractMessage> c : msgClasses) {
				server.getKryo().register(c);
			}
		}

		setListener(_listener);
		server.bind(tcpport, udpport);
		Thread t = new Thread(server);
		t.setDaemon(true);
		t.start();
	}


	public void setListener(IMessageServerListener listener) {

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

			public void connected(Connection connection) {
				connection.setIdleThreshold(timeout);
				connection.setTimeout(timeout);

				listener.connectionAdded(connection.getID(), connection);
			}

			public void disconnected(Connection connection) {
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
		kryo.register(int[].class);
		kryo.register(int[][].class);
		kryo.register(java.util.LinkedList.class);
		kryo.register(java.util.ArrayList.class);
		kryo.register(com.jme3.bounding.BoundingBox.class);
		kryo.register(com.jme3.bounding.BoundingSphere.class);

		// Messages
		kryo.register(MyAbstractMessage.class);
		kryo.register(PingMessage.class);
		kryo.register(JoinGameRequestMessage.class);
		kryo.register(GameSuccessfullyJoinedMessage.class);
		kryo.register(PlayerInputMessage.class);
		kryo.register(NewEntityMessage.class);
		kryo.register(EntityUpdateMessage.class);
		kryo.register(EntityUpdateData.class);
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
		kryo.register(AvatarStatusMessage.class);
		kryo.register(GameOverMessage.class);
		kryo.register(PlaySoundMessage.class);
		kryo.register(ModelBoundsMessage.class);
		kryo.register(ShowMessageMessage.class);
		kryo.register(JoinGameFailedMessage.class);
		kryo.register(SetAvatarMessage.class);
		kryo.register(AbilityActivatedMessage.class);
		kryo.register(NewEntityData.class);
		kryo.register(ClientReloadRequestMessage.class);
		kryo.register(GameLogMessage.class);
		kryo.register(NumEntitiesMessage.class);
		kryo.register(HelloMessage.class);
		kryo.register(AbilityReloadingMessage.class);
	}


	@Override
	public int getNumClients() {
		return server.getConnections().length;
	}


	public static boolean isPacketDropped() {
		return Globals.RELEASE_MODE == false && Globals.PCENT_DROPPED_PACKETS > 0 && NumberFunctions.rnd(0, 100) < Globals.PCENT_DROPPED_PACKETS;
	}


	@Override
	public void sendMessageToClient(final ClientData client, final MyAbstractMessage msg) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Sending to client: " + msg);
		}
		this.sendMessage(client.getPlayerID(), msg);
	}


	private void sendMessage(final int id, final MyAbstractMessage msg) {
		if (Globals.RELEASE_MODE) {

			if (msg.isTCP()) {
				server.sendToTCP(id, msg);
			} else {
				server.sendToUDP(id, msg);
			}

		} else {

			if (msg.isTCP()) {
				server.sendToTCP(id, msg); // todo - delay
			} else {
				if (!KryonetGameServer.isPacketDropped()) {
					if (Globals.MAX_ARTIFICIAL_COMMS_DELAY == 0) {
						server.sendToUDP(id, msg);
					} else {
						Thread t = new Thread("CommsDelayThread") {

							@Override
							public void run() {
								Functions.sleep(NumberFunctions.rnd(Globals.MIN_ARTIFICIAL_COMMS_DELAY, Globals.MAX_ARTIFICIAL_COMMS_DELAY));
								server.sendToUDP(id, msg);
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
		server.close();
	}


}
