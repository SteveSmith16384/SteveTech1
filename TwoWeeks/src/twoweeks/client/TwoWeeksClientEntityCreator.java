package twoweeks.client;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.netmessages.NewEntityMessage;

import twoweeks.abilities.PlayersMachineGun;
import twoweeks.entities.AIBullet;
import twoweeks.entities.AISoldier;
import twoweeks.entities.Floor;
import twoweeks.entities.GenericStaticModel;
import twoweeks.entities.MercClientAvatar;
import twoweeks.entities.MercEnemyAvatar;
import twoweeks.entities.PlayersBullet;
import twoweeks.entities.Terrain1;

public class TwoWeeksClientEntityCreator {

	public static final int SOLDIER_AVATAR = 1;
	public static final int TERRAIN1 = 2;
	public static final int FLOOR = 3;
	public static final int GENERIC_STATIC_MODEL = 4;
	public static final int CRATE = 5;
	public static final int PLAYER_BULLET = 7;
	public static final int MACHINE_GUN = 8;
	public static final int AI_SOLDIER = 10;
	public static final int MAP_BORDER = 11;
	public static final int EXPLOSION_EFFECT = 15;
	public static final int DEBUGGING_SPHERE = 16;
	public static final int AI_BULLET = 17;


	public TwoWeeksClientEntityCreator() {
	}


	public static String TypeToString(int type) {
		switch (type) {
		case SOLDIER_AVATAR: return "Avatar";
		case FLOOR: return "FLOOR";
		case CRATE: return "CRATE";
		case PLAYER_BULLET: return "PLAYER_LASER_BULLET";
		case MACHINE_GUN: return "LASER_RIFLE";
		case MAP_BORDER: return "INVISIBLE_MAP_BORDER";
		case AI_BULLET: return "AI_LASER_BULLET";
		default: return "Unknown (" + type + ")";
		}
	}


	public IEntity createEntity(AbstractGameClient game, NewEntityMessage msg) {
		/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Creating " + TypeToString(msg.type));
		}*/
		int id = msg.entityID;
		Vector3f pos = (Vector3f)msg.data.get("pos");

		switch (msg.type) {
		case SOLDIER_AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			int side = (int)msg.data.get("side");
			float moveSpeed = (float)msg.data.get("moveSpeed");
			float jumpForce = (float)msg.data.get("jumpForce");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new MercClientAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side, moveSpeed, jumpForce);
				//game.getCamera().lookAt(pos.add(Vector3f.UNIT_X), Vector3f.UNIT_Y); // Look somewhere
				Vector3f look = new Vector3f(15f, 1f, 15f);
				game.getCamera().lookAt(look, Vector3f.UNIT_Y); // Look somewhere
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new MercEnemyAvatar(game, SOLDIER_AVATAR, playerID, id, pos.x, pos.y, pos.z, side);
				return avatar;
			}
		}

		case FLOOR:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String name = (String)msg.data.get("name");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, name, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex);
			return floor;
		}

		case GENERIC_STATIC_MODEL:
		{
			String name = (String)msg.data.get("name");
			String modelFile = (String)msg.data.get("modelFile");
			String tex = (String)msg.data.get("tex");
			float height = (float)msg.data.get("height");
			Quaternion q = (Quaternion) msg.data.get("q");
			GenericStaticModel generic = new GenericStaticModel(game, id, TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, name, modelFile, height, tex, pos.x, pos.y, pos.z, q);
			return generic;
		}

		case TERRAIN1:
		{
			Terrain1 floor = new Terrain1(game, id, pos.x, pos.y, pos.z);
			return floor;
		}

		case MACHINE_GUN:
		{
			int ownerid = (int)msg.data.get("ownerid");
			if (game.currentAvatar != null && ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
				AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
				int num = (int)msg.data.get("num");
				PlayersMachineGun gl = new PlayersMachineGun(game, id, owner, num, null);
				return gl;
			}
			return null;

		}

		case PLAYER_BULLET:
		{
			int containerID = (int) msg.data.get("containerID");
			int side = (int) msg.data.get("side");
			IEntityContainer<AbstractPlayersBullet> irac = (IEntityContainer<AbstractPlayersBullet>)game.entities.get(containerID);
			PlayersBullet bullet = new PlayersBullet(game, id, irac, side, null);
			return bullet;
		}

		case AI_BULLET:
		{
			int side = (int) msg.data.get("side");
			int shooterID = (int) msg.data.get("shooterID");
			IEntity shooter = game.entities.get(shooterID);
			Vector3f dir = (Vector3f) msg.data.get("dir");
			AIBullet bullet = new AIBullet(game, id, side, pos.x, pos.y, pos.z, shooter, dir);
			return bullet;
		}

		case AI_SOLDIER:
		{
			int side = (int)msg.data.get("side");
			AISoldier z = new AISoldier(game, id, pos.x, pos.y, pos.z, side);
			return z;
		}

		/*case MAP_BORDER:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f dir = (Vector3f)msg.data.get("dir");
			float size = (float)msg.data.get("size");
			MapBorder hill = new MapBorder(game, id, pos.x, pos.y, pos.z, size, dir);
			return hill;
		}

		case CRATE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			SpaceCrate crate = new SpaceCrate(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, 0); // Give def rotation of 0, since it will get rotated anyway
			return crate;
		}
*/
		case DEBUGGING_SPHERE:
		{
			DebuggingSphere sphere = new DebuggingSphere(game, id, DEBUGGING_SPHERE, pos.x, pos.y, pos.z, true, false);
			return sphere;
		}

		default:
			throw new RuntimeException("Unknown entity type for creation: " + msg.type);
		}
	}

}