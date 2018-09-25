package boxwars;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.ValidateClientSettings;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

import boxwars.entities.BoxWarsClientAvatar;
import boxwars.entities.BoxWarsOtherPlayersAvatar;
import boxwars.entities.Floor;
import boxwars.entities.PlayersBullet;
import boxwars.weapons.PlayersGun;

public class BoxWarsClient extends AbstractGameClient {

	private AbstractCollisionValidator collisionValidator = new AbstractCollisionValidator();

	public static void main(String[] args) {
		try {
			AbstractGameClient app = new BoxWarsClient();
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}

	}


	private BoxWarsClient() {
		super(new ValidateClientSettings("BoxWars", "key", 1), "Box Wars", null, 25, 200, 10000, 1f);
		this.connect("localhost", BoxWarsServer.PORT, false);
		start();
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return collisionValidator.canCollide(a, b);
	}


	@Override
	protected Class[] getListofMessageClasses() {
		return null;
	}


	@Override
	public void disconnectedCode() {
		System.exit(0);
	}
	
	
	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient game, NewEntityData msg) {
		int id = msg.entityID;

		switch (msg.type) {
		case BoxWarsServer.AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			byte side = (byte)msg.data.get("side");
			Vector3f pos = (Vector3f)msg.data.get("pos");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new BoxWarsClientAvatar(game, id, game.input, game.getCamera(), id, pos.x, pos.y, pos.z, side);
				return avatar;
			} else {
				// Create an avatar for another player, since we don't control this one
				AbstractOtherPlayersAvatar avatar = new BoxWarsOtherPlayersAvatar(game, id, pos.x, pos.y, pos.z, side);
				return avatar;
			}
		}

		case BoxWarsServer.FLOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, null);
			return floor;
		}

		case BoxWarsServer.GUN:
		{
			int ownerid = (int)msg.data.get("ownerid");
			byte num = (byte)msg.data.get("num");
			int playerID = (int)msg.data.get("playerID");
			PlayersGun gl = new PlayersGun(game, id, playerID, null, ownerid, num, null);
			return gl;
		}

		case BoxWarsServer.BULLET:
		{
			int playerID = (int) msg.data.get("playerID");
			if (playerID != game.getPlayerID()) {
				byte side = (byte) msg.data.get("side");
				int shooterId =  (int) msg.data.get("shooterID");
				IEntity shooter = game.entities.get(shooterId);
				Vector3f startPos = (Vector3f) msg.data.get("startPos");
				Vector3f dir = (Vector3f) msg.data.get("dir");
				PlayersBullet snowball = new PlayersBullet(game, game.getNextEntityID(), playerID, shooter, startPos, dir, side, null); // Notice we generate our own entity id
				return snowball;
			} else {
				return null;
			}
		}

		default:
			return null;
		}
	}

}
