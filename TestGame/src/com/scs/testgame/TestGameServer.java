package com.scs.testgame;

import java.io.IOException;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.Grenade;
import com.scs.stevetech1.entities.LaserBullet;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractClientEntityCreator;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.weapons.LaserRifle;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.TestGameServerAvatar;
import com.scs.testgame.entities.Wall;
import com.scs.testgame.models.HouseModel;

public class TestGameServer extends AbstractGameServer {

	public static void main(String[] args) {
		try {
			AbstractGameServer app = new TestGameServer();
			app.setPauseOnLostFocus(false);
			app.start(JmeContext.Type.Headless);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public TestGameServer() throws IOException {
		super(new GameOptions("Test Game", 1, 999, 10*1000, 5*60*1000, 10*1000, Globals.GAME_IP_ADDRESS, Globals.GAME_PORT, Globals.LOBBY_IP_ADDRESS, Globals.LOBBY_PORT));
	}


	@Override
	public Vector3f getAvatarStartPosition(AbstractAvatar avatar) {
		return new Vector3f(3f, 0.6f, 3f + (avatar.playerID*2));	
	}


	protected void createGame() {
		new Floor(this, getNextEntityID(), 0, 0, 0, 30, .5f, 30, "Textures/floor015.png", null);
		//new Crate(this, getNextEntityID(), 8, 2, 8, 1, 1, 1f, "Textures/crate.png", 45);
		//new Crate(this, getNextEntityID(), 8, 5, 8, 1, 1, 1f, "Textures/crate.png", 65);
		new Wall(this, getNextEntityID(), 0, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
		new Wall(this, getNextEntityID(), 10, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
		new Wall(this, getNextEntityID(), 20, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
		new Wall(this, getNextEntityID(), 30, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 270);

		//new MovingTarget(this, getNextEntityID(), 2, 2, 10, 1, 1, 1, "Textures/seamless_bricks/bricks2.png", 0);
		
		//new FlatFloor(this, getNextEntityID(), 5, .1f, 5, 3, 3, "Textures/crate.png");
		
		//new RoamingZombie(this, getNextEntityID(), 2, 2, 10);
		
		new HouseModel(this, getNextEntityID(), 20, 0, 20, 0);
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid, int side) {
		return new TestGameServerAvatar(this, client.getPlayerID(), client.remoteInput, entityid, side);
	}


	@Override
	protected IEntity createEntity(int type, int entityid, int side, IRequiresAmmoCache irac) {
		switch (type) {
		case AbstractClientEntityCreator.LASER_BULLET:
			return new LaserBullet(this, entityid, irac);
			
		case AbstractClientEntityCreator.GRENADE:
			return new Grenade(this, entityid, irac);
			
		default:
			return super.createEntity(type, entityid, side, irac);
		}
	}


	@Override
	protected void equipAvatar(AbstractServerAvatar avatar) {
		//IAbility abilityGun = new HitscanRifle(this, getNextEntityID(), avatar, 0);
		//IAbility abilityGun = new GrenadeLauncher(this, getNextEntityID(), avatar, 0);
		IAbility abilityGun = new LaserRifle(this, getNextEntityID(), avatar, 0);
		this.addEntity(abilityGun);
		
		/* 
			this.abilityOther = new JetPac(this, 1);// BoostFwd(this, 1);//getRandomAbility(this);
		game.addEntity(abilityOther);
		}*/

		
	}


}
