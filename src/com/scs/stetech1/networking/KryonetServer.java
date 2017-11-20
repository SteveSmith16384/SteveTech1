package com.scs.stetech1.networking;

import com.esotericsoftware.kryonet.Server;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.server.ClientData;
import com.scs.stetech1.server.Settings;

import java.io.IOException;

import com.esotericsoftware.kryonet.Listener;

public class KryonetServer implements IMessageServer {
	
	private IMessageServerListener listener;
	private Server server;

	public KryonetServer(IMessageServerListener _listener) throws IOException {
		listener = _listener;
		
		server = new Server();
		server.bind(Settings.TCP_PORT, Settings.UDP_PORT);
		//server.addListener(this);

	}

	@Override
	public int getNumClients() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void sendMessageToAll(MyAbstractMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessageToClient(ClientData client, MyAbstractMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
