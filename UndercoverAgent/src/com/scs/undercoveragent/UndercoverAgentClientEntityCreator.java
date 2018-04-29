package com.scs.undercoveragent;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.entities.DebuggingBox;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.undercoveragent.entities.BigTreeWithLeaves;
import com.scs.undercoveragent.entities.Igloo;
import com.scs.undercoveragent.entities.InvisibleMapBorder;
import com.scs.undercoveragent.entities.MountainMapBorder;
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

public class UndercoverAgentClientEntityCreator { //extends AbstractClientEntityCreator {

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
	public static final int DEBUGGING_SPHERE = 16;
	public static final int DEBUGGING_BOX = 17;

	public UndercoverAgentClientEntityCreator() {
		super();
	}


	public static String TypeToString(int type) {
		switch (type) {
		case AVATAR: return "Avatar";
		case FLOOR: return "FLOOR";
		case IGLOO: return "IGLOO";
		case SNOW_HILL_1: return "SNOW_HILL_1";
		case STATIC_SNOWMAN: return "STATIC_SNOWMAN";
		case SNOW_TREE_1: return "SNOW_TREE_1";
		case SNOW_TREE_2: return "SNOW_TREE_2";
		case BIG_TREE_WITH_LEAVES: return "BIG_TREE_WITH_LEAVES";
		case SNOWBALL_LAUNCHER: return "SNOWBALL_LAUNCHER";
		case SNOWBALL_BULLET: return "SNOWBALL_BULLET";
		case INVISIBLE_MAP_BORDER: return "MAP_BORDER";
		case SNOW_HILL_2: return "SNOW_HILL_2";
		case SNOW_HILL_3: return "SNOW_HILL_3";
		case SNOW_HILL_4: return "SNOW_HILL_4";
		case DEBUGGING_SPHERE: return "DEBUGGING_SPHERE";
		default: return "Unknown (" + type + ")";
		}
	}

	
	//@Override
	public IEntity createEntity(AbstractGameClient game, NewEntityData msg) {
		/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Creating " + TypeToString(msg.type));
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
				AbstractClientAvatar avatar = new SnowmanClientAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side, moveSpeed, jumpForce);
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new SnowmanEnemyAvatar(game, AVATAR, playerID, id, pos.x, pos.y, pos.z, side);
				return avatar;
			}
		}

		case FLOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			SnowFloor floor = new SnowFloor(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex);
			return floor;
		}

		case IGLOO:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			Igloo igloo = new Igloo(game, id, pos.x, pos.y, pos.z, q);
			return igloo;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOW_HILL_1:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowHill1 hill = new SnowHill1(game, id, pos.x, pos.y, pos.z, q);
			return hill;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOW_HILL_2:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowHill2 hill = new SnowHill2(game, id, pos.x, pos.y, pos.z, q);
			return hill;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOW_HILL_3:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowHill3 hill = new SnowHill3(game, id, pos.x, pos.y, pos.z, q);
			return hill;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOW_HILL_4:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowHill4 hill = new SnowHill4(game, id, pos.x, pos.y, pos.z, q);
			return hill;  //crate.getMainNode().getWorldTranslation();
		}

		case STATIC_SNOWMAN:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			StaticSnowman snowman = new StaticSnowman(game, id, pos.x, pos.y, pos.z, q);
			return snowman;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOW_TREE_1:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowTree1 tree = new SnowTree1(game, id, pos.x, pos.y, pos.z, q);
			return tree;
		}

		case SNOW_TREE_2:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			SnowTree2 tree = new SnowTree2(game, id, pos.x, pos.y, pos.z, q);
			return tree;
		}

		case BIG_TREE_WITH_LEAVES:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			BigTreeWithLeaves tree = new BigTreeWithLeaves(game, id, pos.x, pos.y, pos.z, q);
			return tree;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOWBALL_LAUNCHER: 
		{
			int ownerid = (int)msg.data.get("ownerid");
			//if (game.currentAvatar != null) { // We might not have an avatar yet
			//	if (ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
				//	AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
					int num = (int)msg.data.get("num");
					int playerID = (int)msg.data.get("playerID");
					SnowballLauncher gl = new SnowballLauncher(game, id, playerID, null, ownerid, num, null);
					return gl;
				/*}
			}
			return null;*/
		}

		case SNOWBALL_BULLET:
		{
			int containerID = (int) msg.data.get("containerID");
			int playerID = (int) msg.data.get("playerID");
			int side = (int) msg.data.get("side");
			IEntityContainer<AbstractPlayersBullet> irac = (IEntityContainer<AbstractPlayersBullet>)game.entities.get(containerID);
			SnowballBullet snowball = new SnowballBullet(game, id, playerID, irac, side, null);
			return snowball;
		}

		case INVISIBLE_MAP_BORDER:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f dir = (Vector3f)msg.data.get("dir");
			float size = (float)msg.data.get("size");
			InvisibleMapBorder hill = new InvisibleMapBorder(game, id, pos.x, pos.y, pos.z, size, dir);
			return hill;
		}

		case MOUNTAIN_MAP_BORDER:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f dir = (Vector3f)msg.data.get("dir");
			float size = (float)msg.data.get("size");
			MountainMapBorder hill = new MountainMapBorder(game, id, pos.x, pos.y, pos.z, size, dir);
			return hill;
		}

		case DEBUGGING_SPHERE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			DebuggingSphere hill = new DebuggingSphere(game, id, DEBUGGING_SPHERE, pos.x, pos.y, pos.z, true, false);
			return hill;
		}

		case DEBUGGING_BOX:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			DebuggingBox hill = new DebuggingBox(game, DEBUGGING_SPHERE, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, true);
			return hill;
		}

		default:
			throw new RuntimeException("Unknown entity type: " + msg.type);
		}
	}
}

