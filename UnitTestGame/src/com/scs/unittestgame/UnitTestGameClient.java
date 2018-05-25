package com.scs.unittestgame;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;
import com.scs.unittestgame.entities.ClientAvatarEntity;
import com.scs.unittestgame.entities.EnemyAvatarEntity;
import com.scs.unittestgame.entities.McGuffinEntity;
import com.scs.unittestgame.entities.UnitTestAbility;

public class UnitTestGameClient extends AbstractGameClient {

	public static void main(String[] args) {
		try {
			new UnitTestGameClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public UnitTestGameClient() {
		super("UnitTest", "key", "Unit Test", null, "localhost", UnitTestGameServer.PORT, 25, 200, 10000, 1f);

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
	protected IHUD createAndGetHUD() {
		return new DummyHUD();
	}

	@Override
	protected void playerHasWon() {

	}


	@Override
	protected void playerHasLost() {

	}


	@Override
	protected void gameIsDrawn() {

	}


	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg) {
		int id = msg.entityID;
		Vector3f pos = null;
		if (msg.data != null) { // McGuffins don't have a pos
			pos = (Vector3f)msg.data.get("pos");
		}

		switch (msg.type) {
		case UnitTestGameServer.MCGUFFIN_ID:
			return new McGuffinEntity(client, msg.entityID);
		case UnitTestGameServer.AVATAR_ID:
			int playerID = (int)msg.data.get("playerID");
			if (playerID == client.playerID) {
				return new ClientAvatarEntity(client, playerID, id, pos.x, pos.y, pos.z, 0);
			} else {
				return new EnemyAvatarEntity(client, id, pos.x, pos.y, pos.z, 0);
			}
		case UnitTestGameServer.ABILITY_ID:
			playerID = (int)msg.data.get("playerID");
			int avatarID = (int)msg.data.get("avatarID");
			return new UnitTestAbility(client, id, playerID, null, avatarID);
		default:
			throw new RuntimeException("Unknown type: " + msg.type);

		}
	}


	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		Globals.p("Game status changed to " + newStatus);

	}

	@Override
	protected Spatial getPlayersWeaponModel() {
		return null;
	}

}
