package boxwars;

import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.netmessages.NewEntityData;

public class BoxWarsClient extends AbstractGameClient {
	
	public BoxWarsClient() {
		super("BoxWars", "Box Wars", null, "localhost", BoxWarsServer.PORT, 25, 200, 10000, 1f);
		start();
		}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Class[] getListofMessageClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IHUD getHUD() {
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
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg) {
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


	@Override
	public int getNumEntities() {
		// TODO Auto-generated method stub
		return 0;
	}


}
