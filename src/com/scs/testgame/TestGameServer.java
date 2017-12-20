package com.scs.testgame;

import java.io.IOException;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.data.GameOptions;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.entities.AbstractServerAvatar;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.ClientData;
import com.scs.testgame.entities.Crate;
import com.scs.testgame.entities.FlatFloor;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.Grenade;
import com.scs.testgame.entities.MovingTarget;
import com.scs.testgame.entities.TestGameServerAvatar;
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
		new Wall(this, getNextEntityID(), 10, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
		new Wall(this, getNextEntityID(), 20, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);

		new MovingTarget(this, getNextEntityID(), 2, 2, 10, 1, 1, 1, "Textures/seamless_bricks/bricks2.png", 0);
		
		new FlatFloor(this, getNextEntityID(), 3, .1f, 3, 2, 2, "Textures/crate.png");
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid, int side) {
		return new TestGameServerAvatar(this, client.getPlayerID(), client.remoteInput, entityid, side);	
	}


	@Override
	protected IEntity createEntity(int type, int entityid, int side) {
		switch (type) {
		case TestGameEntityCreator.GRENADE:
			return new Grenade(this, entityid);
		default:
			throw new RuntimeException("Unknown entity type: " + type);
		}
	}


}
