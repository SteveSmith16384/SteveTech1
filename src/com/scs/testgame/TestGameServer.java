package com.scs.testgame;

import java.io.IOException;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.stetech1.data.GameOptions;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.entities.ServerPlayersAvatar;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.ClientData;
import com.scs.testgame.entities.Crate;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.MovingTarget;
import com.scs.testgame.entities.TestGameServerPlayersAvatar;
import com.scs.testgame.entities.Wall;

public class TestGameServer extends AbstractGameServer {

	public static void main(String[] args) {
		try {
			AbstractGameServer app = new TestGameServer();
			app.setPauseOnLostFocus(false);
			app.start(JmeContext.Type.Headless);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public TestGameServer() throws IOException {
		super(new GameOptions(1, 999));
	}


	@Override
	public Vector3f getAvatarStartPosition(AbstractAvatar avatar) {
		return new Vector3f(3f, 15f, 3f + avatar.playerID);	
	}


	protected void createGame() {
		new Floor(this, getNextEntityID(), 0, 0, 0, 30, .5f, 30, "Textures/floor015.png", null);
		new Crate(this, getNextEntityID(), 8, 2, 8, 1, 1, 1f, "Textures/crate.png", 45);
		//new Crate(this, getNextEntityID(), 8, 5, 8, 1, 1, 1f, "Textures/crate.png", 65);
		new Wall(this, getNextEntityID(), 0, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
		new Wall(this, getNextEntityID(), 1, 0, 11, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);

		new MovingTarget(this, getNextEntityID(), 2, 2, 10, 1, 1, 1, "Textures/seamless_bricks/bricks2.png", 0);
	}


	@Override
	protected ServerPlayersAvatar createPlayersAvatarEntity(ClientData client, int entityid, int side) {
		return new TestGameServerPlayersAvatar(this, client.getPlayerID(), client.remoteInput, entityid, side);	
	}


}
