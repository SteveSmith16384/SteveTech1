package com.scs.moonbaseassault.client;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.abilities.LaserRifle;
import com.scs.moonbaseassault.entities.AISoldier;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.DestroyedComputer;
import com.scs.moonbaseassault.entities.ExplosionEffectEntity;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.Grenade;
import com.scs.moonbaseassault.entities.MapBorder;
import com.scs.moonbaseassault.entities.LaserBullet;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.moonbaseassault.entities.SlidingDoor;
import com.scs.moonbaseassault.entities.SoldierClientAvatar;
import com.scs.moonbaseassault.entities.SoldierEnemyAvatar;
import com.scs.moonbaseassault.entities.SpaceCrate;
import com.scs.moonbaseassault.models.Spaceship1;
import com.scs.moonbaseassault.weapons.GrenadeLauncher;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.netmessages.NewEntityMessage;

public class MoonbaseAssaultClientEntityCreator { //extends AbstractClientEntityCreator {

	public static final int SOLDIER_AVATAR = 1;
	public static final int COMPUTER = 2;
	public static final int FLOOR = 3;
	public static final int DOOR = 4;
	public static final int CRATE = 5;
	public static final int WALL = 6;
	public static final int LASER_BULLET = 7;
	public static final int LASER_RIFLE = 8;
	public static final int SPACESHIP1 = 9;
	public static final int AI_SOLDIER = 10;
	public static final int MAP_BORDER = 11;
	public static final int DESTROYED_COMPUTER = 12;
	public static final int GRENADE = 13;
	public static final int GRENADE_LAUNCHER = 14;
	public static final int EXPLOSION_EFFECT = 15;
	public static final int DEBUGGING_SPHERE = 16;


	public MoonbaseAssaultClientEntityCreator() {
	}


	public static String TypeToString(int type) {
		switch (type) {
		case SOLDIER_AVATAR: return "Avatar";
		case COMPUTER: return "COMPUTER";
		case FLOOR: return "FLOOR";
		case DOOR: return "DOOR";
		case CRATE: return "CRATE";
		case WALL: return "WALL";
		case LASER_BULLET: return "LASER_BULLET";
		case LASER_RIFLE: return "LASER_RIFLE";
		case SPACESHIP1: return "SPACESHIP1";
		case MAP_BORDER: return "INVISIBLE_MAP_BORDER";
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
		case SOLDIER_AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			int side = (int)msg.data.get("side");
			Vector3f pos = (Vector3f)msg.data.get("pos");
			float moveSpeed = (float)msg.data.get("moveSpeed");
			float jumpForce = (float)msg.data.get("jumpForce");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new SoldierClientAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side, moveSpeed, jumpForce);
				//game.getCamera().lookAt(pos.add(Vector3f.UNIT_X), Vector3f.UNIT_Y); // Look somewhere
				Vector3f look = new Vector3f(15f, 1f, 15f);
				game.getCamera().lookAt(look, Vector3f.UNIT_Y); // Look somewhere
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new SoldierEnemyAvatar(game, SOLDIER_AVATAR, playerID, id, pos.x, pos.y, pos.z, side);
				return avatar;
			}
		}

		case FLOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String name = (String)msg.data.get("name");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, name, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex);
			return floor;
		}

		case WALL:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			float w = (float)msg.data.get("w");
			float h = (float)msg.data.get("h");
			float d = (float)msg.data.get("d");
			String tex = (String)msg.data.get("tex");
			//float rot = (Float)msg.data.get("rot");
			MoonbaseWall wall = new MoonbaseWall(game, id, pos.x, pos.y, pos.z, w, h, d, tex);
			return wall;
		}

		case LASER_RIFLE:
		{
			int ownerid = (int)msg.data.get("ownerid");
			if (game.currentAvatar != null && ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
				AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
				int num = (int)msg.data.get("num");
				LaserRifle gl = new LaserRifle(game, id, owner, num, null);
				return gl;
			}
			return null;

		}

		case LASER_BULLET:
		{
			int containerID = (int) msg.data.get("containerID");
			int side = (int) msg.data.get("side");
			IEntityContainer<LaserBullet> irac = (IEntityContainer<LaserBullet>)game.entities.get(containerID);
			LaserBullet bullet = new LaserBullet(game, id, irac, side, null);
			return bullet;
		}

		case DOOR:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			float w = (float)msg.data.get("w");
			float h = (float)msg.data.get("h");
			String tex = (String)msg.data.get("tex");
			float rot = (Float)msg.data.get("rot");
			SlidingDoor wall = new SlidingDoor(game, id, pos.x, pos.y, pos.z, w, h, tex, rot);
			return wall;
		}

		case COMPUTER:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Computer computer = new Computer(game, id, pos.x, pos.y, pos.z);
			return computer;
		}

		case DESTROYED_COMPUTER:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			DestroyedComputer dcomputer = new DestroyedComputer(game, id, pos.x, pos.y, pos.z);
			return dcomputer;
		}

		case SPACESHIP1:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Quaternion q = (Quaternion)msg.data.get("quat");
			Spaceship1 spaceship1 = new Spaceship1(game, id, pos.x, pos.y, pos.z, q);
			return spaceship1;
		}

		case AI_SOLDIER:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			int side = (int)msg.data.get("side");
			AISoldier z = new AISoldier(game, id, pos.x, pos.y, pos.z, side);
			return z;
		}

		case MAP_BORDER:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f dir = (Vector3f)msg.data.get("dir");
			float size = (float)msg.data.get("size");
			MapBorder hill = new MapBorder(game, id, pos.x, pos.y, pos.z, size, dir);
			return hill;
		}

		case GRENADE_LAUNCHER: 
		{
			int ownerid = (int)msg.data.get("ownerid");
			if (game.currentAvatar != null) { // We might not have an avatar yet
			if (ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
				AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
				int num = (int)msg.data.get("num");
				GrenadeLauncher gl = new GrenadeLauncher(game, id, owner, num, null);
				return gl;
			}
			}
			return null;
		}

		case GRENADE:
		{
			int containerID = (int) msg.data.get("containerID");
			int side = (int) msg.data.get("side");
			IEntityContainer<Grenade> irac = (IEntityContainer<Grenade>)game.entities.get(containerID);
			Grenade snowball = new Grenade(game, id, irac, side, null);
			return snowball;
		}

		case CRATE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			SpaceCrate crate = new SpaceCrate(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, 0); // Give def rotation of 0, since it will get rotated anyway
			return crate;
		}

		case EXPLOSION_EFFECT:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			ExplosionEffectEntity expl = new ExplosionEffectEntity(game, id, pos);
			return expl;
		}

		case DEBUGGING_SPHERE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			DebuggingSphere hill = new DebuggingSphere(game, DEBUGGING_SPHERE, id, pos.x, pos.y, pos.z, true);
			return hill;
		}

		default:
			throw new RuntimeException("Unknown entity type for creation: " + msg.type);
		}
	}

}