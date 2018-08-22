package boxwars;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

import boxwars.entities.BoxWarsClientAvatar;
import boxwars.entities.BoxWarsEnemyAvatar;
import boxwars.entities.PlayersBullet;
import boxwars.entities.Floor;
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
		super("BoxWars", "key", "Box Wars", null, 25, 200, 10000, 1f);
		this.connect(this, "localhost", BoxWarsServer.PORT, false);
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
	protected IEntity actuallyCreateEntity(AbstractGameClient game, NewEntityData msg) {
		int id = msg.entityID;

		switch (msg.type) {
		case BoxWarsServer.AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			int side = (int)msg.data.get("side");
			Vector3f pos = (Vector3f)msg.data.get("pos");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new BoxWarsClientAvatar(game, id, game.input, game.getCamera(), id, pos.x, pos.y, pos.z, side);
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new BoxWarsEnemyAvatar(game, id, pos.x, pos.y, pos.z, side);
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
			int num = (int)msg.data.get("num");
			int playerID = (int)msg.data.get("playerID");
			PlayersGun gl = new PlayersGun(game, id, playerID, null, ownerid, num, null);
			return gl;
		}

		case BoxWarsServer.BULLET:
		{
			int containerID = (int) msg.data.get("containerID");
			int playerID = (int) msg.data.get("playerID");
			int side = (int) msg.data.get("side");
			IEntityContainer<AbstractPlayersBullet> irac = (IEntityContainer<AbstractPlayersBullet>)game.entities.get(containerID);
			PlayersBullet snowball = new PlayersBullet(game, id, playerID, irac, side, null);
			return snowball;
		}

		default:
			return null;
		}
	}

	
	@Override
	protected Spatial getPlayersWeaponModel() {
		return null;
	}


}
