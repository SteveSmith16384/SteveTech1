package com.scs.stevetech1.shared;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.Grenade;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.weapons.GrenadeLauncher;
import com.scs.stevetech1.weapons.HitscanRifle;

public abstract class AbstractClientEntityCreator {

	public static final int AVATAR = 1;
	public static final int DEBUGGING_SPHERE = 2;
	public static final int MOVING_TARGET = 3;
	public static final int LASER_BULLET = 4;
	public static final int GRENADE = 5;
	public static final int GRENADE_LAUNCHER = 6;
	public static final int HITSCAN_RIFLE = 7;
	public static final int LASER_RIFLE = 8;

	//private AbstractGameClient game;

	public AbstractClientEntityCreator() {//AbstractGameClient client) {
		//game = client;
	}


	public IEntity createEntity(AbstractGameClient game, NewEntityMessage msg) {
		int id = msg.entityID;

		switch (msg.type) {
		case GRENADE:
		{
			int containerID = (int) msg.data.get("containerID");
			IRequiresAmmoCache<Grenade> irac = (IRequiresAmmoCache<Grenade>)game.entities.get(containerID);
			Grenade grenade = new Grenade(game, id, irac);
			return grenade;
		}

		case DEBUGGING_SPHERE:
		{
			Vector3f pos = (Vector3f)msg.data.get("pos");
			DebuggingSphere laser = new DebuggingSphere(game, id, pos.x, pos.y, pos.z, true);
			return laser;
		}

		case GRENADE_LAUNCHER: 
		{
			int ownerid = (int)msg.data.get("ownerid");
			if (ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
				AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
				int num = (int)msg.data.get("num");
				GrenadeLauncher gl = new GrenadeLauncher(game, id, owner, num);
				return gl;
			}
			return null;
		}

		/*case LASER_RIFLE:
	{

	}*/

		case HITSCAN_RIFLE:
		{
			int ownerid = (int)msg.data.get("ownerid");
			if (ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
				AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
				int num = (int)msg.data.get("num");
				HitscanRifle gl = new HitscanRifle(game, id, owner, num);
				owner.addAbility(gl, num);
				return gl;
			}
			return null;
		}

		default:
			throw new RuntimeException("todo");
		}
	}

}