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
import com.scs.undercoveragent.entities.Igloo;
import com.scs.undercoveragent.entities.InvisibleMapBorder;
import com.scs.undercoveragent.entities.MountainMapBorder;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.entities.SnowTree1;
import com.scs.undercoveragent.entities.SnowmanServerAvatar;
import com.scs.undercoveragent.entities.StaticSnowman;
import com.scs.undercoveragent.weapons.SnowballLauncher;

import ssmith.lang.NumberFunctions;
import ssmith.util.MyProperties;

public class UndercoverAgentServer extends AbstractGameServer {

	private int mapSize;

	public static void main(String[] args) {
		try {
			MyProperties props = null;
			if (args.length > 0) {
				props = new MyProperties(args[0]);
			} else {
				props = new MyProperties();
				Globals.p("Warning: No config file specified");
			}
			String gameIpAddress = props.getPropertyAsString("gameIpAddress", "localhost");
			int gamePort = props.getPropertyAsInt("gamePort", 6143);
			String lobbyIpAddress = props.getPropertyAsString("lobbyIpAddress", "localhost");
			int lobbyPort = props.getPropertyAsInt("lobbyPort", 6144);

			int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			int sendUpdateIntervalMillis = props.getPropertyAsInt("sendUpdateIntervalMillis", 40);
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);
			float gravity = props.getPropertyAsFloat("gravity", -5);
			float aerodynamicness = props.getPropertyAsFloat("aerodynamicness", 0.99f);

			// Game specific
			int mapSize = props.getPropertyAsInt("mapSize", 20);

			startLobbyServer(lobbyPort, timeoutMillis); // Start the lobby in the same process, why not?  Feel from to comment this line out and run it seperately.  If you want a lobby.

			new UndercoverAgentServer(mapSize, 
					gameIpAddress, gamePort, lobbyIpAddress, lobbyPort, 
					tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void startLobbyServer(int lobbyPort, int timeout) {
		Thread r = new Thread("LobbyServer") {

			@Override
			public void run() {
				try {
					new UndercoverAgentLobbyServer(lobbyPort, timeout);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		r.start();


	}


	public UndercoverAgentServer(int _mapSize, 
			String gameIpAddress, int gamePort, String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int sendUpdateIntervalMillis, int clientRenderDelayMillis, int timeoutMillis, float gravity, float aerodynamicness) throws IOException {
		super(new GameOptions("Undercover Agent", 1, 999, 10*1000, 60*1000, 10*1000, 
				gameIpAddress, gamePort, lobbyIpAddress, lobbyPort, 
				10, 5), tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness);

		mapSize = _mapSize;
	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		float startHeight = 3f; // Must be higher than the highest entity so they don't start "inside" something else
		if (Globals.PLAYERS_START_IN_CORNER) {
			avatar.setWorldTranslation(new Vector3f(3f, startHeight, 3f + (avatar.playerID*2)));
		} else {
			// Find a random position
			SimpleRigidBody<PhysicalEntity> collider;
			do {
				float x = NumberFunctions.rndFloat(2, mapSize-3);
				float z = NumberFunctions.rndFloat(2, mapSize-3);
				avatar.setWorldTranslation(x, startHeight, z);
				collider = avatar.simpleRigidBody.checkForCollisions();
			} while (collider != null);
		}
		Globals.p("Player starting at " + avatar.getWorldTranslation());
	}


	protected void createGame() {
		if (Globals.EMPTY_MAP) {
			// Do nothing
		} else if (Globals.MODELS_IN_GRID) {
			for (int z=1 ; z<mapSize-1 ; z++) {
				for (int x=1 ; x<mapSize-1 ; x++) {
					StaticSnowman snowman = new StaticSnowman(this, getNextEntityID(), mapSize/2, 0, mapSize/2, JMEFunctions.getRotation(-1, 0));
					this.actuallyAddEntity(snowman);
					snowman.setWorldTranslation(x, z);
				}
			}
		} else {
			// Place snowman
			int numSnowmen = mapSize;
			for (int i=0 ; i<numSnowmen ; i++) {
				StaticSnowman snowman = new StaticSnowman(this, getNextEntityID(), mapSize/2, 0, mapSize/2, JMEFunctions.getRotation(-1, 0));
				this.addEntityToRandomPosition(snowman);
				//Globals.p("Placed " + i + " snowmen.");
			}

			// Place trees
			int numTrees = mapSize;
			for (int i=0 ; i<numTrees ; i++) {
				SnowTree1 tree1 = new SnowTree1(this, getNextEntityID(), mapSize/2, 0, mapSize/2, JMEFunctions.getRotation(-1, 0));
				this.addEntityToRandomPosition(tree1);
				//Globals.p("Placed " + i + " tree.");
			}

			// Place igloos
			int numIgloos = mapSize/2;
			for (int i=0 ; i<numIgloos ; i++) {
				Igloo igloo = new Igloo(this, getNextEntityID(), mapSize/2, 0, mapSize/2, JMEFunctions.getRotation(-1, 0));
				this.addEntityToRandomPosition(igloo);
				//Globals.p("Placed " + i + " igloo.");
			}
		}

		// Place floor last so the snowmen don't collide with it when being placed
		SnowFloor floor = new SnowFloor(this, getNextEntityID(), 0, 0, 0, mapSize, .5f, mapSize, "Textures/snow.jpg");
		this.actuallyAddEntity(floor);

		// To make things easier for the server and client, we surround the map with a colliding but invisible wall.  We also place non-colliding but complex shapes in the same place.

		// Map border
		InvisibleMapBorder borderL = new InvisibleMapBorder(this, getNextEntityID(), 0, 0, 0, mapSize, Vector3f.UNIT_Z);
		this.actuallyAddEntity(borderL);
		InvisibleMapBorder borderR = new InvisibleMapBorder(this, getNextEntityID(), mapSize, 0, 0, mapSize, Vector3f.UNIT_Z);
		this.actuallyAddEntity(borderR);
		InvisibleMapBorder borderBack = new InvisibleMapBorder(this, getNextEntityID(), 0, 0, mapSize, mapSize, Vector3f.UNIT_X);
		this.actuallyAddEntity(borderBack);
		InvisibleMapBorder borderFront = new InvisibleMapBorder(this, getNextEntityID(), 0, 0, -InvisibleMapBorder.BORDER_WIDTH, mapSize, Vector3f.UNIT_X);
		this.actuallyAddEntity(borderFront);

		MountainMapBorder mborderL = new MountainMapBorder(this, getNextEntityID(), 0, 0, 0, mapSize, Vector3f.UNIT_Z);
		this.actuallyAddEntity(mborderL); // works
		MountainMapBorder mborderR = new MountainMapBorder(this, getNextEntityID(), mapSize+InvisibleMapBorder.BORDER_WIDTH, 0, 0, mapSize, Vector3f.UNIT_Z);
		this.actuallyAddEntity(mborderR); // works
		MountainMapBorder mborderBack = new MountainMapBorder(this, getNextEntityID(), 0, 0, mapSize, mapSize, Vector3f.UNIT_X);
		this.actuallyAddEntity(mborderBack);
		MountainMapBorder mborderFront = new MountainMapBorder(this, getNextEntityID(), 0, 0, -InvisibleMapBorder.BORDER_WIDTH, mapSize, Vector3f.UNIT_X);
		this.actuallyAddEntity(mborderFront);
	}


	private void addEntityToRandomPosition(PhysicalEntity entity) {
		float x = NumberFunctions.rndFloat(2, mapSize-3);
		float z = NumberFunctions.rndFloat(2, mapSize-3);
		//StaticSnowman snowman = new StaticSnowman(this, getNextEntityID(), x, 0, z, JMEFunctions.getRotation(-1, 0));
		this.actuallyAddEntity(entity);
		SimpleRigidBody<PhysicalEntity> collider = entity.simpleRigidBody.checkForCollisions();
		while (collider != null) {
			x = NumberFunctions.rndFloat(2, mapSize-3);
			z = NumberFunctions.rndFloat(2, mapSize-3);
			entity.setWorldTranslation(x, z);
			collider = entity.simpleRigidBody.checkForCollisions();
		}
		// randomly rotate snowman
		JMEFunctions.rotateToDirection(entity.getMainNode(), NumberFunctions.rnd(0,  359));
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		SnowmanServerAvatar avatar = new SnowmanServerAvatar(this, client, client.getPlayerID(), client.remoteInput, entityid);
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
			//Globals.p("Collision between " + pea + " and " + peb);
		}

		super.collisionOccurred(a, b, point);

	}


	@Override
	public float getAvatarStartHealth(AbstractAvatar avatar) {
		return 1;
	}


	@Override
	protected int getWinningSide() {
		int highestScore = -1;
		int winningSide = -1;
		boolean draw = false;
		for(ClientData c : super.clients.values()) {
			if (c.getScore() > highestScore) {
				winningSide = c.side;
				highestScore = c.getScore();
				draw = false;
			} else if (c.getScore() == highestScore) {
				draw = true;
			}
		}
		if (draw) {
			return -1;
		}
		return winningSide;
	}


	@Override
	public float getAvatarMoveSpeed(AbstractAvatar avatar) {
		return 3f;
	}


	@Override
	public float getAvatarJumpForce(AbstractAvatar avatar) {
		return 2f;
	}


}
