package com.scs.unittestgame;

import java.io.IOException;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.ValidateClientSettings;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.unittestgame.entities.McGuffinEntity;
import com.scs.unittestgame.entities.ServerAvatarEntity;

public class UnitTestGameServer extends AbstractGameServer {

	public static final int PORT = 16384;

	// Entioty types
	public static final int MCGUFFIN_ID = 1;
	public static final int AVATAR_ID = 2;
	public static final int ABILITY_ID = 3;

	public static void main(String[] args) {
		try {
			new UnitTestGameServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public UnitTestGameServer() throws IOException {
		super(new ValidateClientSettings("UnitTest", 1d, "key"), 
				new GameOptions(25, 50, 200, Integer.MAX_VALUE, 10*1000, 60*1000, 10*1000, "localhost", PORT, 10, 5));

		super.physicsController.setGravity(0); // stop things falling

		start(JmeContext.Type.Headless);

	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return false;
	}

	@Override
	protected Class[] getListofMessageClasses() {
		return null;
	}


	@Override
	public byte getSideForPlayer(ClientData client) {
		return 0;
	}

	@Override
	protected void createGame() {
		//this.gameData = new SimpleGameData(nextGameID.getAndAdd(1));

		for (int i=0 ; i<RunAll.NUM_ENTITIES ; i++) {
			McGuffinEntity e = new McGuffinEntity(this, this.getNextEntityID());
			this.actuallyAddEntity(e);
		}

	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		avatar.setWorldTranslation(new Vector3f(0, 10, 0)); // stop them falling off edge
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		return new ServerAvatarEntity(this, client, entityid);
	}


	@Override
	protected byte getWinningSideAtEnd() {
		return 0;
	}


	@Override
	public int getMinPlayersRequiredForGame() {
		return 1;
	}

/*
	@Override
	protected String getSideName(int side) {
		return "Unit Test";
	}
	*/
}

