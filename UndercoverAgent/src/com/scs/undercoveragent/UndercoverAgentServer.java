package com.scs.undercoveragent;

import java.io.IOException;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IAbility;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.entities.Snowball;
import com.scs.undercoveragent.entities.SnowmanServerAvatar;
import com.scs.undercoveragent.weapons.SnowballLauncher;

public class UndercoverAgentServer extends AbstractGameServer {

	public static void main(String[] args) {
		try {
			AbstractGameServer app = new UndercoverAgentServer();
			app.setPauseOnLostFocus(false);
			app.start(JmeContext.Type.Headless);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public UndercoverAgentServer() throws IOException {
		super(new GameOptions("Undercover Agent", 1, 999, 10*1000, 5*60*1000, 10*1000, Globals.GAME_IP_ADDRESS, Globals.GAME_PORT, Globals.LOBBY_IP_ADDRESS, Globals.LOBBY_PORT));
	}


	@Override
	public Vector3f getAvatarStartPosition(AbstractAvatar avatar) {
		return new Vector3f(3f, 0.6f, 3f + (avatar.playerID*2));	
	}


	protected void createGame() {
		new SnowFloor(this, getNextEntityID(), 0, 0, 0, 30, .5f, 30, "Textures/floor015.png", null);

		//new Crate(this, getNextEntityID(), 8, 5, 8, 1, 1, 1f, "Textures/crate.png", 65);
		//new Wall(this, getNextEntityID(), 30, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 270);

		//new MovingTarget(this, getNextEntityID(), 2, 2, 10, 1, 1, 1, "Textures/seamless_bricks/bricks2.png", 0);

		//new RoamingZombie(this, getNextEntityID(), 2, 2, 10);
		
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid, int side) {
		return new SnowmanServerAvatar(this, client.getPlayerID(), client.remoteInput, entityid, side);
	}


	@Override
	protected IEntity createEntity(int type, int entityid, int side, IRequiresAmmoCache irac) {
		switch (type) {
		case UndercoverAgentClientEntityCreator.SNOWBALL:
			return new Snowball(this, entityid, irac);
			
		default:
			return super.createEntity(type, entityid, side, irac);
		}
	}


	@Override
	protected void equipAvatar(AbstractServerAvatar avatar) {
		//IAbility abilityGun = new HitscanRifle(this, getNextEntityID(), avatar, 0);
		//IAbility abilityGun = new GrenadeLauncher(this, getNextEntityID(), avatar, 0);
		IAbility abilityGun = new SnowballLauncher(this, getNextEntityID(), avatar, 0);
		this.addEntity(abilityGun);
		
		/* 
			this.abilityOther = new JetPac(this, 1);// BoostFwd(this, 1);//getRandomAbility(this);
		game.addEntity(abilityOther);
		}*/

		
	}


}
