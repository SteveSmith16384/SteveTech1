package com.scs.testgame;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IRequiresAmmoCache;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.entities.AbstractEnemyAvatar;
import com.scs.stetech1.entities.ClientPlayersAvatar;
import com.scs.stetech1.entities.DebuggingSphere;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.weapons.GrenadeLauncher;
import com.scs.stetech1.weapons.HitscanRifle;
import com.scs.testgame.entities.Crate;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.Grenade;
import com.scs.testgame.entities.MovingTarget;
import com.scs.testgame.entities.TestGameClientPlayersAvatar;
import com.scs.testgame.entities.TestGameEnemyPlayersAvatar;
import com.scs.testgame.entities.Wall;

/*
 * This is only used client-side.
 */
public class TestGameEntityCreator {

	private TestGameClient game;

	public TestGameEntityCreator(TestGameClient _game) {
		game =_game;
	}


	public IEntity createEntity(NewEntityMessage msg) {
		Settings.p("Creating " + EntityTypes.getName(msg.type));
		int id = msg.entityID;// (Integer)msg.data.get("id");

		switch (msg.type) {
		case EntityTypes.AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			int side = (int)msg.data.get("side");
			Vector3f pos = (Vector3f)msg.data.get("pos");

			if (playerID == game.playerID) {
				ClientPlayersAvatar avatar = new TestGameClientPlayersAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side);
				//game.avatar = avatar;
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new TestGameEnemyPlayersAvatar(game, playerID, id, pos.x, pos.y, pos.z);
				return avatar;
			}
		}

		case EntityTypes.FLOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, null);
			return floor;
		}

		case EntityTypes.CRATE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Quaternion rot = (Quaternion)msg.data.get("quat"); // todo - use this
			Crate crate = new Crate(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, 0); // Give def rotation of 0, since it will get rotated anyway
			return crate;  //crate.getMainNode().getWorldTranslation();
		}

		case EntityTypes.WALL:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			float w = (float)msg.data.get("w");
			float h = (float)msg.data.get("h");
			String tex = (String)msg.data.get("tex");
			float rot = (Float)msg.data.get("rot");
			Wall wall = new Wall(game, id, pos.x, pos.y, pos.z, w, h, tex, rot);
			return wall;
		}

		/*case EntityTypes.LASER_BULLET: todo
		{
			String tex = (String)msg.data.get("tex");
			float rot = (Float)msg.data.get("rot");
			LaserBullet laser = new LaserBullet(game, id, pos.x, pos.y, pos.z, w, h, tex, rot);
			return laser;
		}*/

		case EntityTypes.GRENADE:
		{
			//int side = (int) msg.data.get("side");
			int containerID = (int) msg.data.get("containerID");
			IRequiresAmmoCache<Grenade> irac = (IRequiresAmmoCache<Grenade>)game.entities.get(containerID);
			Grenade grenade = new Grenade(game, id, irac);
			/*if (side == game.side) {
				IRequiresAmmoCache<Grenade> irac = (IRequiresAmmoCache<Grenade>)game.entities.get(containerID);
				irac.addToCache(grenade);
			}*/
			return grenade;
		}

		case EntityTypes.DEBUGGING_SPHERE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			DebuggingSphere laser = new DebuggingSphere(game, id, pos.x, pos.y, pos.z, true);
			return laser;
		}

		case EntityTypes.MOVING_TARGET:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f) msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Quaternion quat = (Quaternion)msg.data.get("quat"); // todo - use this
			//float rot = (Float)msg.data.get("rot");
			MovingTarget laser = new MovingTarget(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, 0);
			return laser;
		}

		case EntityTypes.GRENADE_LAUNCHER: 
		{
			int ownerid = (int)msg.data.get("ownerid");
			if (ownerid == game.avatar.id) { // Don't care about other's abilities?
				AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
				int num = (int)msg.data.get("num");
				GrenadeLauncher gl = new GrenadeLauncher(game, id, owner, num);
				owner.addAbility(gl, num);
				return gl;
			}
			return null;
		}

		case EntityTypes.LASER_RIFLE:
		{
			// todo
		}

		case EntityTypes.HITSCAN_RIFLE:
		{
			int ownerid = (int)msg.data.get("ownerid");
			if (ownerid == game.avatar.id) { // Don't care about other's abilities?
				AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
				int num = (int)msg.data.get("num");
				HitscanRifle gl = new HitscanRifle(game, id, owner, num);
				owner.addAbility(gl, num);
				return gl;
			}
			return null;
		}

		default:
			throw new RuntimeException("Unknown entity type: " + EntityTypes.getName(msg.type));
		}
	}
}
