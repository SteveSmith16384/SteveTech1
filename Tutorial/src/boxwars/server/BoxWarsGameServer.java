package boxwars.server;

import java.io.IOException;

import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;

public class BoxWarsGameServer extends AbstractGameServer {
	
	public static final int PORT = 16384;
	
	public BoxWarsGameServer() throws IOException {
			super("BoxWars", 
					new GameOptions(10*1000, 60*1000, 10*1000, "localhost", PORT, 10, 5), 
					25, 50, 200, 10000);
			start(JmeContext.Type.Headless);

	}
	

	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int getWinningSide() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Class[] getListofMessageClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean doWeHaveSpaces() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getSide(ClientData client) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void createGame() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getMinPlayersRequiredForGame() {
		return 2;
	}


}
