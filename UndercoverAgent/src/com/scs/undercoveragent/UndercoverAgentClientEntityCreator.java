package com.scs.undercoveragent;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.shared.AbstractClientEntityCreator;
import com.scs.undercoveragent.entities.BigTreeWithLeaves;
import com.scs.undercoveragent.entities.Igloo;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.entities.SnowHill1;
import com.scs.undercoveragent.entities.SnowTree1;
import com.scs.undercoveragent.entities.SnowTree2;
import com.scs.undercoveragent.entities.SnowballBullet;
import com.scs.undercoveragent.entities.SnowmanClientAvatar;
import com.scs.undercoveragent.entities.SnowmanEnemyAvatar;
import com.scs.undercoveragent.entities.StaticSnowman;
import com.scs.undercoveragent.weapons.SnowballLauncher;

public class UndercoverAgentClientEntityCreator extends AbstractClientEntityCreator {

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

	public UndercoverAgentClientEntityCreator() {
		super();
	}


	@Override
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

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new SnowmanClientAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side);
				game.getCamera().lookAt(new Vector3f(15, .5f, 15), Vector3f.UNIT_Y);
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new SnowmanEnemyAvatar(game, playerID, id, pos.x, pos.y, pos.z);
				return avatar;
			}
		}

		case FLOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			SnowFloor floor = new SnowFloor(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, null);
			return floor;
		}

		case IGLOO:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion rot = (Quaternion)msg.data.get("quat"); // todo - use this
			Igloo crate = new Igloo(game, id, pos.x, pos.y, pos.z, 0); // Give def rotation of 0, since it will get rotated anyway
			return crate;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOW_HILL_1:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion rot = (Quaternion)msg.data.get("quat"); // todo - use this
			SnowHill1 crate = new SnowHill1(game, id, pos.x, pos.y, pos.z, 0); // Give def rotation of 0, since it will get rotated anyway
			return crate;  //crate.getMainNode().getWorldTranslation();
		}

		case STATIC_SNOWMAN:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion rot = (Quaternion)msg.data.get("quat"); // todo - use this
			StaticSnowman snowman = new StaticSnowman(game, id, pos.x, pos.y, pos.z, 0); // Give def rotation of 0, since it will get rotated anyway
			return snowman;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOW_TREE_1:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion rot = (Quaternion)msg.data.get("quat"); // todo - use this
			SnowTree1 tree = new SnowTree1(game, id, pos.x, pos.y, pos.z, 0); // Give def rotation of 0, since it will get rotated anyway
			return tree;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOW_TREE_2:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion rot = (Quaternion)msg.data.get("quat"); // todo - use this
			SnowTree2 crate = new SnowTree2(game, id, pos.x, pos.y, pos.z, 0); // Give def rotation of 0, since it will get rotated anyway
			return crate;  //crate.getMainNode().getWorldTranslation();
		}

		case BIG_TREE_WITH_LEAVES:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion rot = (Quaternion)msg.data.get("quat"); // todo - use this
			BigTreeWithLeaves tree = new BigTreeWithLeaves(game, id, pos.x, pos.y, pos.z, 0); // Give def rotation of 0, since it will get rotated anyway
			return tree;  //crate.getMainNode().getWorldTranslation();
		}

		case SNOWBALL_LAUNCHER: 
		{
			int ownerid = (int)msg.data.get("ownerid");
			if (ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
				AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
				int num = (int)msg.data.get("num");
				SnowballLauncher gl = new SnowballLauncher(game, id, owner, num);
				return gl;
			}
			return null;
		}

		case SNOWBALL_BULLET:
		{
			int containerID = (int) msg.data.get("containerID");
			IRequiresAmmoCache<SnowballBullet> irac = (IRequiresAmmoCache<SnowballBullet>)game.entities.get(containerID);
			SnowballBullet snowball = new SnowballBullet(game, id, irac);
			return snowball;
		}


		default:
			return super.createEntity(game, msg);
		}
	}
}

