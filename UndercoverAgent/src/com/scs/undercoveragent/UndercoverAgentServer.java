package com.scs.undercoveragent;

import java.io.IOException;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEFunctions;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IAbility;
import com.scs.undercoveragent.entities.InvisibleMapBorder;
import com.scs.undercoveragent.entities.MountainMapBorder;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.entities.SnowmanServerAvatar;
import com.scs.undercoveragent.entities.StaticSnowman;
import com.scs.undercoveragent.weapons.SnowballLauncher;

import ssmith.lang.NumberFunctions;

public class UndercoverAgentServer extends AbstractGameServer {

	public static void main(String[] args) {
		try {
			startLobbyServer();
			
			AbstractGameServer app = new UndercoverAgentServer();
			app.setPauseOnLostFocus(false);
			app.start(JmeContext.Type.Headless);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private static void startLobbyServer() {
		// Run the lobby server as well
		Thread r = new Thread("LobbyServer") {

			@Override
			public void run() {
				try {
					new UndercoverAgentLobbyServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		r.start();


	}

	
	public UndercoverAgentServer() throws IOException {
		super(new GameOptions("Undercover Agent", 1, 999, 10*1000, 5*60*1000, 10*1000, 
				UndercoverAgentStaticData.GAME_IP_ADDRESS, UndercoverAgentStaticData.GAME_PORT, UndercoverAgentStaticData.LOBBY_IP_ADDRESS, UndercoverAgentStaticData.LOBBY_PORT, 
				10, 5));

		//properties = new GameProperties(PROPS_FILE);
	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		if (Globals.PLAYERS_START_IN_CORNER) {
			avatar.setWorldTranslation(new Vector3f(3f, 1f, 3f + (avatar.playerID*2)));
		} else {
			// Find a random position
			SimpleRigidBody<PhysicalEntity> collider;
			do {
				float x = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
				float z = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
				avatar.setWorldTranslation(x, 2f, z);
				collider = avatar.simpleRigidBody.checkForCollisions();
			} while (collider != null);
		}
		Globals.p("Player starting at " + avatar.getWorldTranslation());
	}


	protected void createGame() {
		/*
		new Igloo(this, getNextEntityID(), 5, 0, 5, JMEFunctions.GetRotation(-1, 0));
		//new SnowHill1(this, getNextEntityID(), 10, 0, 10, 0);
		new StaticSnowman(this, getNextEntityID(), 5, 0, 10, JMEFunctions.GetRotation(-1, 0));
		new SnowTree2(this, getNextEntityID(), 10, 0, 5, JMEFunctions.GetRotation(-1, 0));
		 */

		if (!Globals.EMPTY_MAP) {
			// Place snowman
			int numSnowmen = UndercoverAgentStaticData.MAP_SIZE;
			for (int i=0 ; i<numSnowmen ; i++) {
				//while (numSnowmen > 0) {
				float x = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
				float z = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
				StaticSnowman snowman = new StaticSnowman(this, getNextEntityID(), x, 0, z, JMEFunctions.getRotation(-1, 0));
				this.actuallyAddEntity(snowman);
				SimpleRigidBody<PhysicalEntity> collider = snowman.simpleRigidBody.checkForCollisions();
				while (collider != null) {
					x = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
					z = NumberFunctions.rndFloat(2, UndercoverAgentStaticData.MAP_SIZE-3);
					snowman.setWorldTranslation(x, z);
					collider = snowman.simpleRigidBody.checkForCollisions();
				}
				// randomly rotate snowman
				JMEFunctions.rotateToDirection(snowman.getMainNode(), NumberFunctions.rnd(0,  359));
				Globals.p("Placed " + i + " snowmen.");
			}
		}

		// Place floor last so the snowmen don't collide with it when being placed
		SnowFloor floor = new SnowFloor(this, getNextEntityID(), 0, 0, 0, UndercoverAgentStaticData.MAP_SIZE, .5f, UndercoverAgentStaticData.MAP_SIZE, "Textures/snow.jpg");
		this.actuallyAddEntity(floor);

		// To make things easier for the server and client, we surround the map with a colliding but invisible wall.  We also place non-colliding but complex shapes in the same place.
		
		// Map border
		InvisibleMapBorder borderL = new InvisibleMapBorder(this, getNextEntityID(), 0, 0, 0, UndercoverAgentStaticData.MAP_SIZE, Vector3f.UNIT_Z);
		this.actuallyAddEntity(borderL);
		InvisibleMapBorder borderR = new InvisibleMapBorder(this, getNextEntityID(), UndercoverAgentStaticData.MAP_SIZE, 0, 0, UndercoverAgentStaticData.MAP_SIZE, Vector3f.UNIT_Z);
		this.actuallyAddEntity(borderR);
		InvisibleMapBorder borderBack = new InvisibleMapBorder(this, getNextEntityID(), 0, 0, UndercoverAgentStaticData.MAP_SIZE, UndercoverAgentStaticData.MAP_SIZE, Vector3f.UNIT_X);
		this.actuallyAddEntity(borderBack);
		InvisibleMapBorder borderFront = new InvisibleMapBorder(this, getNextEntityID(), 0, 0, -InvisibleMapBorder.BORDER_WIDTH, UndercoverAgentStaticData.MAP_SIZE, Vector3f.UNIT_X);
		this.actuallyAddEntity(borderFront);

		MountainMapBorder mborderL = new MountainMapBorder(this, getNextEntityID(), 0, 0, 0, UndercoverAgentStaticData.MAP_SIZE, Vector3f.UNIT_Z);
		this.actuallyAddEntity(mborderL); // works
		MountainMapBorder mborderR = new MountainMapBorder(this, getNextEntityID(), UndercoverAgentStaticData.MAP_SIZE+InvisibleMapBorder.BORDER_WIDTH, 0, 0, UndercoverAgentStaticData.MAP_SIZE, Vector3f.UNIT_Z);
		this.actuallyAddEntity(mborderR); // works
		MountainMapBorder mborderBack = new MountainMapBorder(this, getNextEntityID(), 0, 0, UndercoverAgentStaticData.MAP_SIZE, UndercoverAgentStaticData.MAP_SIZE, Vector3f.UNIT_X);
		this.actuallyAddEntity(mborderBack);
		MountainMapBorder mborderFront = new MountainMapBorder(this, getNextEntityID(), 0, 0, -InvisibleMapBorder.BORDER_WIDTH, UndercoverAgentStaticData.MAP_SIZE, Vector3f.UNIT_X);
		this.actuallyAddEntity(mborderFront);
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid, int side) {
		SnowmanServerAvatar avatar = new SnowmanServerAvatar(this, client.getPlayerID(), client.remoteInput, entityid, side);
		//avatar.getMainNode().lookAt(new Vector3f(15, avatar.avatarModel.getCameraHeight(), 15), Vector3f.UNIT_Y); // Look towards the centre

		IAbility abilityGun = new SnowballLauncher(this, getNextEntityID(), avatar, 0);
		this.actuallyAddEntity(abilityGun);

		return avatar;
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


	@Override
	public float getAvatarStartHealth(AbstractAvatar avatar) {
		return 1;
	}


}
