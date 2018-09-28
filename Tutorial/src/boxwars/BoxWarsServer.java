package boxwars;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractSimpleGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

import boxwars.entities.BoxWarsServerAvatar;
import boxwars.entities.Floor;

public class BoxWarsServer extends AbstractSimpleGameServer {

	public static final int PORT = 16384;

	// Entity codes
	public static final int AVATAR = 1;
	public static final int FLOOR = 2;
	public static final int GUN = 3;
	public static final int BULLET = 4;

	private AbstractCollisionValidator collisionValidator = new AbstractCollisionValidator();

	public static void main(String[] args) {
		try {
			new BoxWarsServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private BoxWarsServer() {
		super(PORT);

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
	public int getMinSidesRequiredForGame() {
		return 2; // Need at least two players to start a game
	}


}
