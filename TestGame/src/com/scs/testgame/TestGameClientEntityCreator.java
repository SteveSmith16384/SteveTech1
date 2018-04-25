package com.scs.testgame;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.entities.BulletTrail;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.server.Globals;
import com.scs.testgame.entities.Crate;
import com.scs.testgame.entities.FlatFloor;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.House;
import com.scs.testgame.entities.MovingTarget;
import com.scs.testgame.entities.PlayerLaserBullet;
import com.scs.testgame.entities.RoamingZombie;
import com.scs.testgame.entities.Terrain1;
import com.scs.testgame.entities.TestGameClientAvatar;
import com.scs.testgame.entities.TestGameEnemyAvatar;
import com.scs.testgame.entities.Wall;
import com.scs.testgame.weapons.HitscanRifle;
import com.scs.testgame.weapons.LaserRifle;

/*
 * This is only used client-side.
 */
public class TestGameClientEntityCreator {

	public static final int AVATAR = 1;
	public static final int DEBUGGING_SPHERE = 2;
	public static final int MOVING_TARGET = 3;
	public static final int PLAYER_LASER_BULLET = 4;
	public static final int HITSCAN_RIFLE = 7;
	public static final int LASER_RIFLE = 8;
	public static final int CRATE = 9;
	public static final int FLOOR = 10;
	public static final int FENCE = 11;
	public static final int WALL = 12;
	public static final int FLAT_FLOOR = 13;
	public static final int ZOMBIE = 14;
	public static final int HOUSE = 15;
	public static final int TERRAIN1 = 16;
	public static final int BULLET_TRAIL = 17;


	public TestGameClientEntityCreator() {
		super();
	}


	public IEntity createEntity(AbstractGameClient game, NewEntityMessage msg) {
		/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Creating " + getName(msg.type));
		}*/
		int id = msg.entityID;

		switch (msg.type) {
		case AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			int side = (int)msg.data.get("side");
			Vector3f pos = (Vector3f)msg.data.get("pos");
			float moveSpeed = (float)msg.data.get("moveSpeed");
			float jumpForce = (float)msg.data.get("jumpForce");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new TestGameClientAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side, moveSpeed, jumpForce);
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new TestGameEnemyAvatar(game, playerID, id, pos.x, pos.y, pos.z, side);
				return avatar;
			}
		}

		case DEBUGGING_SPHERE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			DebuggingSphere laser = new DebuggingSphere(game, id, DEBUGGING_SPHERE, pos.x, pos.y, pos.z, false, true);
			return laser;
		}

		case HITSCAN_RIFLE:
		{
			int ownerid = (int)msg.data.get("ownerid");
			//if (ownerid == game.currentAvatar.id) { // Don't care about other's abilities
			//AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
			int playerID = (int)msg.data.get("playerID");
			int num = (int)msg.data.get("num");
			HitscanRifle gl = new HitscanRifle(game, id, playerID, null, ownerid, num, null);
			//owner.addAbility(gl, num);
			return gl;
			//}
			//return null;
		}

		case FLOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, null);
			return floor;
		}

		case TERRAIN1:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Terrain1 floor = new Terrain1(game, id, pos.x, pos.y, pos.z);
			return floor;
		}

		case CRATE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
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
			MovingTarget laser = new MovingTarget(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, 0);
			return laser;
		}

		case ZOMBIE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			RoamingZombie z = new RoamingZombie(game, id, pos.x, pos.y, pos.z);
			return z;
		}

		case HOUSE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion rot = (Quaternion)msg.data.get("quat");
			House house = new House(game, id, pos.x, pos.y, pos.z, 0);
			return house;
		}

		case LASER_RIFLE:
		{
			int ownerid = (int)msg.data.get("ownerid");
			//if (game.currentAvatar != null && ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
			//AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
			int num = (int)msg.data.get("num");
			int playerID = (int)msg.data.get("playerID");
			LaserRifle gl = new LaserRifle(game, id, playerID, null, ownerid, num, null);
			if (Globals.DEBUG_NO_ABILITY0) {
				Globals.p("Got new laser rifle for avatar " + ownerid);
			}
			return gl;
			/*} else {
				if (Globals.DEBUG_NO_ABILITY0) {
					Globals.p("Ignoring new laser rifle " + id);
				}
				return null;
			}*/

		}

		case PLAYER_LASER_BULLET:
		{
			int containerID = (int) msg.data.get("containerID");
			int playerID = (int) msg.data.get("playerID");
			int side = (int) msg.data.get("side");
			Vector3f dir = (Vector3f) msg.data.get("dir");
			IEntityContainer<AbstractPlayersBullet> irac = (IEntityContainer<AbstractPlayersBullet>)game.entities.get(containerID);
			PlayerLaserBullet bullet = new PlayerLaserBullet(game, id, playerID, irac, side, null, dir);
			return bullet;
		}

		case BULLET_TRAIL:
		{
			int shooterID = (int) msg.data.get("shooterID");
			if (shooterID != game.currentAvatarID) {
				int targetID = (int) msg.data.get("targetID");
				Vector3f end = (Vector3f) msg.data.get("end");
				BulletTrail bullet = new BulletTrail(game, id, BULLET_TRAIL, (ICanShoot)game.getEntity(shooterID), (PhysicalEntity)game.getEntity(targetID), end);
				return bullet;
			} else {
				return null; // We create our own bullet trails
			}

		}

		default:
			throw new RuntimeException("Unknown entity type: " + msg.type);
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
		case PLAYER_LASER_BULLET: return "PLAYER_LASER_BULLET";
		//case GRENADE: return "GRENADE";
		//case GRENADE_LAUNCHER: return "GRENADE_LAUNCHER";
		case LASER_RIFLE: return "LASER_RIFLE";
		case HITSCAN_RIFLE: return "HITSCAN_RIFLE";
		case FLAT_FLOOR: return "Flat Floor";
		case ZOMBIE: return "ZOMBIE";
		default: return "UNKNOWN (" + type + ")";
		}
	}

}
