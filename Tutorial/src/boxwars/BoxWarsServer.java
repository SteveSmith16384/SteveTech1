package boxwars;

import java.io.IOException;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.stevetech1.client.ValidateClientSettings;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

import boxwars.entities.BoxWarsServerAvatar;
import boxwars.entities.Floor;

public class BoxWarsServer extends AbstractGameServer {

	// Entity codes
	public static final int AVATAR = 1;
	public static final int FLOOR = 2;
	public static final int GUN = 3;
	public static final int BULLET = 4;

	public static final int PORT = 16384;
	
	private AbstractCollisionValidator collisionValidator = new AbstractCollisionValidator();

	public static void main(String[] args) {
		try {
			new BoxWarsServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private BoxWarsServer() throws IOException {
		super(new ValidateClientSettings("BoxWars", 1d, "key"),
				new GameOptions(25, 50, 200, 10000, 10*1000, 60*1000, 10*1000, "localhost", PORT, 10, 5));
		start(JmeContext.Type.Headless);

	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return collisionValidator.canCollide(a, b);
	}

	@Override
	protected byte getWinningSideAtEnd() {
		return 0;
	}

	@Override
	protected Class[] getListofMessageClasses() {
		return null; // No custom data in messages
	}


	/*
	 * Just use the client id as the side, to easily ensure every player is on a different side. 
	 */
	@Override
	public byte getSideForPlayer(ClientData client) {
		return (byte) client.getPlayerID(); // todo - check > 127
	}

	
	@Override
	protected void createGame() {
		// Just a floor.
		Floor floor = new Floor(this, getNextEntityID(), 0, 0, 0, 30, .5f, 30, "Textures/floor015.png", null);
		this.actuallyAddEntity(floor);
	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		avatar.setWorldTranslation(new Vector3f(3f, 26f, 3f + (avatar.playerID*2)));

	}

	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		return new BoxWarsServerAvatar(this, client, client.remoteInput, entityid);
	}


	@Override
	public int getMinPlayersRequiredForGame() {
		return 2; // Need at least two players to start a game
	}


}
