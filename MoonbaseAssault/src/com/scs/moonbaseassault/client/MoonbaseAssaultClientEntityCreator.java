package com.scs.moonbaseassault.client;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.abilities.LaserRifle;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.LaserBullet;
import com.scs.moonbaseassault.entities.SoldierClientAvatar;
import com.scs.moonbaseassault.entities.SoldierEnemyAvatar;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.server.Globals;

public class MoonbaseAssaultClientEntityCreator { //extends AbstractClientEntityCreator {

	public static final int AVATAR = 1;
	public static final int COMPUTER = 2;
	public static final int FLOOR = 3;
	public static final int DOOR = 4;
	public static final int CRATE = 5;
	public static final int WALL = 6;
	public static final int LASER_BULLET = 7;
	public static final int LASER_RIFLE = 8;

	public MoonbaseAssaultClientEntityCreator() {
	}


	public static String TypeToString(int type) {
		switch (type) {
		case AVATAR: return "Avatar";
		case FLOOR: return "FLOOR";
		default: return "Unknown (" + type + ")";
		}
	}


	//@Override
	public IEntity createEntity(AbstractGameClient game, NewEntityMessage msg) {
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

			if (Globals.DEBUG_TOO_MANY_AVATARS) {
				Globals.p("Creating avatar id " + id + " for " + playerID + " at " + pos);
			}

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new SoldierClientAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side);
				avatar.moveSpeed = moveSpeed;
				avatar.setJumpForce(jumpForce);
				game.getCamera().lookAt(new Vector3f(15, .5f, 15), Vector3f.UNIT_Y);
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new SoldierEnemyAvatar(game, playerID, id, pos.x, pos.y, pos.z);
				return avatar;
			}
		}

		case FLOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex);
			return floor;
		}

		case LASER_RIFLE:
		{
			int ownerid = (int)msg.data.get("ownerid");
			if (ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
				AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
				int num = (int)msg.data.get("num");
				LaserRifle gl = new LaserRifle(game, id, owner, num);
				return gl;
			}
			return null;

		}

		case LASER_BULLET:
		{
			int containerID = (int) msg.data.get("containerID");
			int side = (int) msg.data.get("side");
			IEntityContainer<LaserBullet> irac = (IEntityContainer<LaserBullet>)game.entities.get(containerID);
			LaserBullet bullet = new LaserBullet(game, id, irac, side);
			return bullet;
		}

		default:
			throw new RuntimeException("Todo");
		}
	}

}