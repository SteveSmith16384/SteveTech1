package boxwars;

import java.io.IOException;

import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;

public class BoxWarsServer extends AbstractGameServer {
	
	// Entity codes
	public static final int AVATAR = 1;
	public static final int FLOOR = 2;
	public static final int GUN = 3;
	public static final int BULLET = 4;
	
	public static final int PORT = 16384;
	
	public BoxWarsServer() throws IOException {
			super("BoxWars", 
					new GameOptions(10*1000, 60*1000, 10*1000, "localhost", PORT, 10, 5), 
					25, 50, 200, 10000);
			start(JmeContext.Type.Headless);

	}
	

	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return true;
	}

	@Override
	protected int getWinningSideAtEnd() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Class[] getListofMessageClasses() {
		return null; // No custom data in messages (yet?)
	}

	
	@Override
	public boolean doWeHaveSpaces() {
		return true; // Always room for one more.
	}

	/*
	 * Just use the client id as the side, to easily ensure every player is on a different side. 
	 */
	@Override
	public int getSide(ClientData client) {
		return client.id;
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
		return 2; // Need at least two players
	}


}
