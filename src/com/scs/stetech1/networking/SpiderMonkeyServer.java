package com.scs.stetech1.networking;

import java.io.IOException;

import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.netmessages.PlayerInputMessage;
import com.scs.stetech1.netmessages.PlayerLeftMessage;
import com.scs.stetech1.netmessages.UnknownEntityMessage;
import com.scs.stetech1.server.Settings;

public class SpiderMonkeyServer implements IMessageServer, ConnectionListener, MessageListener<HostedConnection>,  {

	private Server myServer;

	public SpiderMonkeyServer() throws IOException {
		myServer = Network.createServer(Settings.PORT);

		Settings.registerMessages();

		myServer.start();
		myServer.addConnectionListener(this);

		myServer.addMessageListener(this, PingMessage.class);
		myServer.addMessageListener(this, NewPlayerRequestMessage.class);
		myServer.addMessageListener(this, GameSuccessfullyJoinedMessage.class);
		myServer.addMessageListener(this, PlayerInputMessage.class);
		myServer.addMessageListener(this, UnknownEntityMessage.class);
		myServer.addMessageListener(this, NewEntityMessage.class);
		myServer.addMessageListener(this, EntityUpdateMessage.class);
		myServer.addMessageListener(this, PlayerLeftMessage.class);

	}

}
