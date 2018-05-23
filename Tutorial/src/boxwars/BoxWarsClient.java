package boxwars;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;

import boxwars.entities.BoxWarsClientAvatar;
import boxwars.entities.BoxWarsEnemyAvatar;
import boxwars.entities.Floor;

public class BoxWarsClient extends AbstractGameClient {

	public static void main(String[] args) {
		try {
			AbstractGameClient app = new BoxWarsClient();
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}

	}


	private BoxWarsClient() {
		super("BoxWars", "Box Wars", null, "localhost", BoxWarsServer.PORT, 25, 200, 10000, 1f);
		start();
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return true;
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
	protected IEntity actuallyCreateEntity(AbstractGameClient game, NewEntityData msg) {
		int id = msg.entityID;

		switch (msg.type) {
		case BoxWarsServer.AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			int side = (int)msg.data.get("side");
			Vector3f pos = (Vector3f)msg.data.get("pos");
			float moveSpeed = (float)msg.data.get("moveSpeed");
			float jumpForce = (float)msg.data.get("jumpForce");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new BoxWarsClientAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side, moveSpeed, jumpForce);
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
		
		default:
			return null;
		}
	}

	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {

	}

	@Override
	protected Spatial getPlayersWeaponModel() {
		return null;
	}

}
