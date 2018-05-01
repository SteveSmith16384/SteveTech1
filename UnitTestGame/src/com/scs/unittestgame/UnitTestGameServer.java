package com.scs.unittestgame;

import java.io.IOException;

import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.unittestgame.entities.McGuffinEntity;

public class UnitTestGameServer extends AbstractGameServer {

	public static final int PORT = 16384;
	public static final int MCGUFFIN_ID = 1;

	public static void main(String[] args) {
		try {
			new UnitTestGameServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private UnitTestGameServer() throws IOException {
		super("UnitTest", 
				new GameOptions(10*1000, 60*1000, 10*1000, "localhost", PORT, 10, 5), 
				25, 50, 200, 10000);
		start(JmeContext.Type.Headless);

	}


	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		return false;
	}

	@Override
	protected Class[] getListofMessageClasses() {
		return null;
	}

	@Override
	public boolean doWeHaveSpaces() {
		return true;
	}

	@Override
	public int getSide(ClientData client) {
		return 0;
	}

	@Override
	protected void createGame() {
		this.gameData = new SimpleGameData();
		
		for (int i=0 ; i<100 ; i++) {
			McGuffinEntity e = new McGuffinEntity(this, this.getNextEntityID());
			this.actuallyAddEntity(e);
		}

	}

	@Override
	public float getAvatarStartHealth(AbstractAvatar avatar) {
		return 1;
	}

	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		// Do nothing
	}

	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getWinningSide() {
		return 0;
	}

	@Override
	public int getMinPlayersRequiredForGame() {
		return 0;
	}

}
