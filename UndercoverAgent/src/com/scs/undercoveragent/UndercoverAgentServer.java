package com.scs.undercoveragent;

import java.io.IOException;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IAbility;
import com.scs.undercoveragent.entities.MapBorder;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.entities.SnowballBullet;
import com.scs.undercoveragent.entities.SnowmanServerAvatar;
import com.scs.undercoveragent.weapons.SnowballLauncher;

import ssmith.lang.NumberFunctions;

public class UndercoverAgentServer extends AbstractGameServer {

	//private UndercoverAgentGameProperties properties;

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
		super(new GameOptions("Undercover Agent", 1, 999, 10*1000, 5*60*1000, 10*1000, UndercoverAgentStaticData.GAME_IP_ADDRESS, UndercoverAgentStaticData.GAME_PORT, UndercoverAgentStaticData.LOBBY_IP_ADDRESS, UndercoverAgentStaticData.LOBBY_PORT, 5, 5));

		//properties = new GameProperties(PROPS_FILE);
	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		//return new Vector3f(3f, 0.6f, 3f + (avatar.playerID*2));
		SimpleRigidBody<PhysicalEntity> collider;
		do {
			float x = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
			float z = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
			avatar.setWorldTranslation(x, 2f, z);
			collider = avatar.simpleRigidBody.checkForCollisions();
		} while (collider != null);
		/*while (collider != null) {
			x = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
			z = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
			avatar.setWorldTranslation(x, 2f, z);
			collider = avatar.simpleRigidBody.checkForCollisions();
		}*/
		Globals.p("Player starting at " + avatar.getWorldTranslation());
	}


	protected void createGame() {
		// Create border
		/*for (int z=0; z<UndercoverAgentStaticData.MAP_SIZE ; z+=2) {
			for (int x=0; x<UndercoverAgentStaticData.MAP_SIZE ; x+=2) {
				if (x == 0 || z == 0 || x >= UndercoverAgentStaticData.MAP_SIZE-1 || z >= UndercoverAgentStaticData.MAP_SIZE-2) {
					if (NumberFunctions.rnd(0, 1) == 0) {
						SnowHill1 hill = new SnowHill1(this, getNextEntityID(), x, 0, z, JMEFunctions.GetRotation(-1, 0));
					} else {
						SnowHill1 hill = new SnowHill1(this, getNextEntityID(), x, 0, z, JMEFunctions.GetRotation(1, 0));
					}
				} else {
					int rnd = NumberFunctions.rnd(0, 6);
					switch (rnd) {
					}
				}
			}			
		}*/

		/*
		new Igloo(this, getNextEntityID(), 5, 0, 5, JMEFunctions.GetRotation(-1, 0));
		// todo - actually add entities
		//new SnowHill1(this, getNextEntityID(), 10, 0, 10, 0);
		new StaticSnowman(this, getNextEntityID(), 5, 0, 10, JMEFunctions.GetRotation(-1, 0));
		new SnowTree2(this, getNextEntityID(), 10, 0, 5, JMEFunctions.GetRotation(-1, 0));

		// Place snowman
		int numSnowmen = 30;
		for (int i=0 ; i<numSnowmen ; i++) {
			//while (numSnowmen > 0) {
			float x = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
			float z = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
			StaticSnowman snowman = new StaticSnowman(this, getNextEntityID(), x, 0, z, JMEFunctions.GetRotation(-1, 0));
			SimpleRigidBody<PhysicalEntity> collider = snowman.simpleRigidBody.checkForCollisions();
			while (collider != null) {
				x = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
				z = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
				snowman.setWorldTranslation(x, z);
				collider = snowman.simpleRigidBody.checkForCollisions();
			}
			//numSnowmen--;
			Globals.p("Placed " + i + " snowmen.");
		}
		 */
		// Place floor last so the snowmen don't collide with it when being placed
		SnowFloor floor = new SnowFloor(this, getNextEntityID(), 0, 0, 0, UndercoverAgentStaticData.MAP_SIZE, .5f, UndercoverAgentStaticData.MAP_SIZE, "Textures/snow.jpg");
		this.actuallyAddEntity(floor);

		MapBorder border1 = new MapBorder(this, getNextEntityID(), 0, 0, 0, UndercoverAgentStaticData.MAP_SIZE, new Quaternion());
		this.actuallyAddEntity(border1);

	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid, int side) {
		SnowmanServerAvatar avatar = new SnowmanServerAvatar(this, client, client.getPlayerID(), client.remoteInput, entityid, side);
		//avatar.getMainNode().lookAt(new Vector3f(15, avatar.avatarModel.getCameraHeight(), 15), Vector3f.UNIT_Y); // Look towards the centre
		return avatar;
	}


	@Override
	public IEntity createEntity(int type, int entityid, int side, IRequiresAmmoCache irac) {
		switch (type) {
		case UndercoverAgentClientEntityCreator.SNOWBALL_BULLET:
			return new SnowballBullet(this, entityid, irac);

		default:
			return super.createEntity(type, entityid, side, irac);
		}
	}


	@Override
	protected void equipAvatar(AbstractServerAvatar avatar) {
		IAbility abilityGun = new SnowballLauncher(this, getNextEntityID(), avatar, 0);
		this.actuallyAddEntity(abilityGun);

	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b, Vector3f point) {
		PhysicalEntity pea = a.userObject;
		PhysicalEntity peb = b.userObject;

		if (pea instanceof SnowFloor == false && peb instanceof SnowFloor == false) {
			Globals.p("Collision between " + pea + " and " + peb);
		}

		super.collisionOccurred(a, b, point);

	}


}
