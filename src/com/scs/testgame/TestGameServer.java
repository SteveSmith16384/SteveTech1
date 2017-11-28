package com.scs.testgame;

import java.io.IOException;

import com.jme3.system.JmeContext;
import com.scs.stetech1.entities.ServerPlayersAvatar;
import com.scs.stetech1.networking.KryonetServer;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.ClientData;
import com.scs.stetech1.server.Settings;
import com.scs.testgame.entities.Crate;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.TestGameServerPlayersAvatar;
import com.scs.testgame.entities.Wall;

public class TestGameServer extends AbstractGameServer {
	
	public static void main(String[] args) {
		try {
			AbstractGameServer app = new TestGameServer();
			app.setPauseOnLostFocus(false);
			if (!Settings.STAND_ALONE_SERVER) {
				app.start(JmeContext.Type.Headless);
			} else {
				app.start();				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public TestGameServer() throws IOException {
		super(new KryonetServer(Settings.TCP_PORT, Settings.UDP_PORT));// SpiderMonkeyServer(this);
	}


	protected void createGame() {
		new Floor(this, getNextEntityID(), 0, 0, 0, 30, .5f, 30, "Textures/floor015.png", null);
		//new DebuggingSphere(this, getNextEntityID(), 0, 0, 0);
		new Crate(this, getNextEntityID(), 8, 2, 8, 1, 1, 1f, "Textures/crate.png", 45);
		//new Crate(this, getNextEntityID(), 8, 5, 8, 1, 1, 1f, "Textures/crate.png", 65);
		new Wall(this, getNextEntityID(), 0, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
	}


	@Override
	protected ServerPlayersAvatar createPlayersAvatar(ClientData client, int entityid, byte side) {
		return new TestGameServerPlayersAvatar(this, client.getPlayerID(), client.remoteInput, entityid, side);	
	}


}
