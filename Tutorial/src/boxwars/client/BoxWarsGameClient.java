package boxwars.client;

import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.netmessages.NewEntityMessage;

import boxwars.server.BoxWarsGameServer;

public class BoxWarsGameClient extends AbstractGameClient {
	
	public BoxWarsGameClient() {
		super("BoxWars", "Box Wars", null, "localhost", BoxWarsGameServer.PORT, 25, 200, 10000, 1f);
	}


	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Class[] getListofMessageClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IHUD createHUD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void playerHasWon() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void playerHasLost() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void gameIsDrawn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Spatial getPlayersWeaponModel() {
		// TODO Auto-generated method stub
		return null;
	}

}
