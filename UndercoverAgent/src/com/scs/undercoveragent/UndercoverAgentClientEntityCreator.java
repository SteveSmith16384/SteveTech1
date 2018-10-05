package com.scs.undercoveragent;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.ExplosionShard;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;
import com.scs.undercoveragent.entities.BigTreeWithLeaves;
import com.scs.undercoveragent.entities.HealthPack;
import com.scs.undercoveragent.entities.Igloo;
import com.scs.undercoveragent.entities.InvisibleMapBorder;
import com.scs.undercoveragent.entities.MountainMapBorder;
import com.scs.undercoveragent.entities.MovingTargetSnowman;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.entities.SnowHill1;
import com.scs.undercoveragent.entities.SnowHill2;
import com.scs.undercoveragent.entities.SnowHill3;
import com.scs.undercoveragent.entities.SnowHill4;
import com.scs.undercoveragent.entities.SnowTree1;
import com.scs.undercoveragent.entities.SnowTree2;
import com.scs.undercoveragent.entities.SnowballBullet;
import com.scs.undercoveragent.entities.SnowmanClientAvatar;
import com.scs.undercoveragent.entities.SnowmanEnemyAvatar;
import com.scs.undercoveragent.entities.StaticSnowman;
import com.scs.undercoveragent.weapons.SnowballLauncher;

public class UndercoverAgentClientEntityCreator {

	public static final int AVATAR = 1;
	public static final int FLOOR = 2;
	public static final int IGLOO = 3;
	public static final int SNOW_HILL_1 = 4;
	public static final int STATIC_SNOWMAN = 5;
	public static final int SNOW_TREE_1 = 6;
	public static final int SNOW_TREE_2 = 7;
	public static final int BIG_TREE_WITH_LEAVES = 8;
	public static final int SNOWBALL_LAUNCHER = 9;
	public static final int SNOWBALL_BULLET = 10;
	public static final int INVISIBLE_MAP_BORDER = 11;
	public static final int MOUNTAIN_MAP_BORDER = 12;
	public static final int SNOW_HILL_2 = 13;
	public static final int SNOW_HILL_3 = 14;
	public static final int SNOW_HILL_4 = 15;
	public static final int MOVING_TARGET_SNOWMAN = 16;
	public static final int HEALTHPACK = 17;
	public static final int FALLING_SNOWFLAKE = 18;

	public UndercoverAgentClientEntityCreator() {
		super();
	}


	public IEntity createEntity(AbstractGameClient game, NewEntityData msg) {
		int id = msg.entityID;

		Vector3f pos = (Vector3f)msg.data.get("pos");

		switch (msg.type) {
		case AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			byte side = (byte)msg.data.get("side");
			String playersName = (String)msg.data.get("playersName");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new SnowmanClientAvatar(game, id, game.input, game.getCamera(), id, pos.x, pos.y, pos.z, side);
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractOtherPlayersAvatar avatar = new SnowmanEnemyAvatar(game, id, pos.x, pos.y, pos.z, side, playersName);
				return avatar;
			}
		}

		case FLOOR:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			SnowFloor floor = new SnowFloor(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex);
			return floor;
		}

		case IGLOO:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			Igloo igloo = new Igloo(game, id, pos.x, pos.y, pos.z, q);
			return igloo;
		}

		case SNOW_HILL_1:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowHill1 hill = new SnowHill1(game, id, pos.x, pos.y, pos.z, q);
			return hill;
		}

		case SNOW_HILL_2:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowHill2 hill = new SnowHill2(game, id, pos.x, pos.y, pos.z, q);
			return hill;
		}

		case SNOW_HILL_3:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowHill3 hill = new SnowHill3(game, id, pos.x, pos.y, pos.z, q);
			return hill;
		}

		case SNOW_HILL_4:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowHill4 hill = new SnowHill4(game, id, pos.x, pos.y, pos.z, q);
			return hill;
		}

		case STATIC_SNOWMAN:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			StaticSnowman snowman = new StaticSnowman(game, id, pos.x, pos.y, pos.z, q);
			return snowman;
		}

		case MOVING_TARGET_SNOWMAN:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			MovingTargetSnowman snowman = new MovingTargetSnowman(game, id, pos.x, pos.y, pos.z, q, null);
			return snowman;
		}

		case SNOW_TREE_1:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowTree1 tree = new SnowTree1(game, id, pos.x, pos.y, pos.z, q);
			return tree;
		}

		case SNOW_TREE_2:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowTree2 tree = new SnowTree2(game, id, pos.x, pos.y, pos.z, q);
			return tree;
		}

		case BIG_TREE_WITH_LEAVES:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			BigTreeWithLeaves tree = new BigTreeWithLeaves(game, id, pos.x, pos.y, pos.z, q);
			return tree;
		}

		case SNOWBALL_LAUNCHER: 
		{
			int ownerid = (int)msg.data.get("ownerid");
			byte num = (byte)msg.data.get("num");
			int playerID = (int)msg.data.get("playerID");
			SnowballLauncher gl = new SnowballLauncher(game, id, playerID, null, ownerid, num, null);
			return gl;
		}

		case SNOWBALL_BULLET:
		{
			int playerID = (int) msg.data.get("playerID");
			if (playerID != game.getPlayerID()) {
				byte side = (byte) msg.data.get("side");
				int shooterId =  (int) msg.data.get("shooterID");
				IEntity shooter = game.entities.get(shooterId);
				Vector3f startPos = (Vector3f) msg.data.get("startPos");
				Vector3f dir = (Vector3f) msg.data.get("dir");
				SnowballBullet snowball = new SnowballBullet(game, game.getNextEntityID(), playerID, shooter, startPos, dir, side, null); // Notice we generate our own ID
				return snowball;
			} else {
				return null; // it's our bullet, which we've already created locally
			}
		}

		case INVISIBLE_MAP_BORDER:
		{
			Vector3f dir = (Vector3f)msg.data.get("dir");
			float size = (float)msg.data.get("size");
			InvisibleMapBorder hill = new InvisibleMapBorder(game, id, pos.x, pos.y, pos.z, size, dir);
			return hill;
		}

		case MOUNTAIN_MAP_BORDER:
		{
			Vector3f dir = (Vector3f)msg.data.get("dir");
			float size = (float)msg.data.get("size");
			MountainMapBorder hill = new MountainMapBorder(game, id, pos.x, pos.y, pos.z, size, dir);
			return hill;
		}

		case Globals.DEBUGGING_SPHERE:
		{
			DebuggingSphere hill = new DebuggingSphere(game, id, pos.x, pos.y, pos.z, true, false);
			return hill;
		}

		case Globals.EXPLOSION_SHARD:
		{
			Vector3f forceDirection = (Vector3f) msg.data.get("forceDirection");
			float size = (float) msg.data.get("size");
			String tex = (String) msg.data.get("tex");
			ExplosionShard expl = new ExplosionShard(game, pos.x, pos.y, pos.z, size, forceDirection, tex);
			return expl;
		}

		case HEALTHPACK:
		{
			HealthPack healthPack = new HealthPack(game, id, pos.x, pos.y, pos.z);
			return healthPack;
		}

		default:
			throw new RuntimeException("Unknown entity type: " + msg.type);
		}
	}
}

