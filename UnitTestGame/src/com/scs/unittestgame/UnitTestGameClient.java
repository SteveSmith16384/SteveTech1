package com.scs.unittestgame;

import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;

public class UnitTestGameClient extends AbstractGameClient {

	public static void main(String[] args) {
		try {
			new UnitTestGameClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private UnitTestGameClient() {
		super("UnitTest", "Unit Test", null, "localhost", UnitTestGameServer.PORT, 25, 200, 10000, 1f);

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
	protected IHUD getHUD() {
		return null;
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
		// TODO Auto-generated method stub
		return null;
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
