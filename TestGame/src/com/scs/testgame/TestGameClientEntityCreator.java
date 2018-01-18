package com.scs.testgame;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractClientEntityCreator;
import com.scs.testgame.entities.Crate;
import com.scs.testgame.entities.FlatFloor;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.MovingTarget;
import com.scs.testgame.entities.TestGameClientAvatar;
import com.scs.testgame.entities.TestGameEnemyAvatar;
import com.scs.testgame.entities.Wall;
import com.scs.testgame.models.HouseModel;

/*
 * This is only used client-side.
 */
public class TestGameClientEntityCreator extends AbstractClientEntityCreator {

	//private TestGameClient game;

	public static final int CRATE = 100;
	public static final int FLOOR = 101;
	public static final int FENCE = 102;
	public static final int WALL = 103;
	public static final int FLAT_FLOOR = 104;
	public static final int ZOMBIE = 105;
	public static final int HOUSE = 106;
	
	
	public TestGameClientEntityCreator() {
		super();
		
		//game =_game;
	}


	@Override
	public IEntity createEntity(AbstractGameClient game, NewEntityMessage msg) {
		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Creating " + getName(msg.type));
		}
		int id = msg.entityID;

		switch (msg.type) {
		case AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			int side = (int)msg.data.get("side");
			Vector3f pos = (Vector3f)msg.data.get("pos");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new TestGameClientAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side);
				//game.avatar = avatar;
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new TestGameEnemyAvatar(game, playerID, id, pos.x, pos.y, pos.z);
				return avatar;
			}
		}

		case FLOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, null);
			return floor;
		}

		case CRATE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Quaternion rot = (Quaternion)msg.data.get("quat"); // todo - use this
			Crate crate = new Crate(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, 0); // Give def rotation of 0, since it will get rotated anyway
			return crate;  //crate.getMainNode().getWorldTranslation();
		}

		case WALL:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			float w = (float)msg.data.get("w");
			float h = (float)msg.data.get("h");
			String tex = (String)msg.data.get("tex");
			float rot = (Float)msg.data.get("rot");
			Wall wall = new Wall(game, id, pos.x, pos.y, pos.z, w, h, tex, rot);
			return wall;
		}

		/*case EntityTypes.LASER_BULLET:
		{
			String tex = (String)msg.data.get("tex");
			float rot = (Float)msg.data.get("rot");
			LaserBullet laser = new LaserBullet(game, id, pos.x, pos.y, pos.z, w, h, tex, rot);
			return laser;
		}*/

		case FLAT_FLOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			FlatFloor floor = new FlatFloor(game, id, pos.x, pos.y, pos.z, size.x, size.z, tex);
			return floor;
		}

		case MOVING_TARGET:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f) msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Quaternion quat = (Quaternion)msg.data.get("quat"); // todo - use this
			//float rot = (Float)msg.data.get("rot");
			MovingTarget laser = new MovingTarget(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, 0);
			return laser;
		}

		/*case ZOMBIE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			RoamingZombie z = new RoamingZombie(game, id, pos.x, pos.y, pos.z);
			return z;
		}*/

		case HOUSE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion rot = (Quaternion)msg.data.get("quat"); // todo - use this
			HouseModel house = new HouseModel(game, id, pos.x, pos.y, pos.z, 0);
			return house;
		}

		default:
			return super.createEntity(game, msg);
		}
	}
	
	
	public static String getName(int type) {
		switch (type) {
		case AVATAR: return "Avatar";
		case CRATE: return "CRATE";
		case FLOOR: return "FLOOR";
		case FENCE: return "FENCE";
		case WALL: return "WALL";
		case DEBUGGING_SPHERE: return "DEBUGGING_SPHERE";
		case MOVING_TARGET: return "MOVING_TARGET";
		case LASER_BULLET: return "LASER_BULLET";
		case GRENADE: return "GRENADE";
		case GRENADE_LAUNCHER: return "GRENADE_LAUNCHER";
		case LASER_RIFLE: return "LASER_RIFLE";
		case HITSCAN_RIFLE: return "HITSCAN_RIFLE";
		case FLAT_FLOOR: return "Flat Floor";
		case ZOMBIE: return "ZOMBIE";
		default: return "UNKNOWN (" + type + ")";
		}
	}

}
