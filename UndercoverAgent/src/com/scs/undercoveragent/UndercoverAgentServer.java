package com.scs.undercoveragent;

import java.io.IOException;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;
import com.scs.stevetech1.shared.IAbility;
import com.scs.undercoveragent.entities.Igloo;
import com.scs.undercoveragent.entities.InvisibleMapBorder;
import com.scs.undercoveragent.entities.MountainMapBorder;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.entities.SnowTree1;
import com.scs.undercoveragent.entities.SnowTree2;
import com.scs.undercoveragent.entities.MovingTargetSnowman;
import com.scs.undercoveragent.entities.SnowmanServerAvatar;
import com.scs.undercoveragent.entities.StaticSnowman;
import com.scs.undercoveragent.weapons.SnowballLauncher;

import ssmith.lang.NumberFunctions;
import ssmith.util.MyProperties;

public class UndercoverAgentServer extends AbstractGameServer {

	public static final String NAME = "Undercover Agent";
	public static final String GAME_ID = "Undercover Agent";

	private int mapSize;
	private AbstractCollisionValidator collisionValidator = new AbstractCollisionValidator();

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

			int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			int sendUpdateIntervalMillis = props.getPropertyAsInt("sendUpdateIntervalMillis", 40);
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);

			// Game specific
			int mapSize = props.getPropertyAsInt("mapSize", 20);

			new UndercoverAgentServer(mapSize, 
					gameIpAddress, gamePort, //lobbyIpAddress, lobbyPort, 
					tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis);//, gravity, aerodynamicness);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private UndercoverAgentServer(int _mapSize, 
			String gameIpAddress, int gamePort, 
			int tickrateMillis, int sendUpdateIntervalMillis, int clientRenderDelayMillis, int timeoutMillis) throws IOException {
		super(GAME_ID, 1d, "key", new GameOptions(tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis, 10*1000, 5*60*1000, 10*1000, 
				gameIpAddress, gamePort, 
				10, 5));

		mapSize = _mapSize;
		start(JmeContext.Type.Headless);

	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		float startHeight = 3f;
		if (Globals.PLAYERS_START_IN_CORNER) {
			avatar.setWorldTranslation(new Vector3f(3f, startHeight, 3f + (avatar.playerID*2)));
		} else {
			// Find a random position
			do {
				float x = NumberFunctions.rndFloat(2, mapSize-3);
				float z = NumberFunctions.rndFloat(2, mapSize-3);
				avatar.setWorldTranslation(x, startHeight, z);
			} while (avatar.simpleRigidBody.checkForCollisions(false).size() > 0);
		}
		Globals.p("Player starting at " + avatar.getWorldTranslation());
	}


	@Override
	protected void createGame() {
		if (Globals.EMPTY_MAP) {
			// Do nothing
		} else if (Globals.FEW_MODELS) {
			for (int z=1 ; z<mapSize-1 ; z+=2) {
				StaticSnowman snowman = new StaticSnowman(this, getNextEntityID(), mapSize/2, 0, mapSize/2, JMEAngleFunctions.getYAxisRotation(-1, 0));
				this.actuallyAddEntity(snowman);
				snowman.setWorldTranslation(3, z+2);
			}
		} else {
			// Place snowman
			int numSnowmen = mapSize;
			for (int i=0 ; i<numSnowmen ; i++) {
				StaticSnowman snowman = new StaticSnowman(this, getNextEntityID(), mapSize/2, 0, mapSize/2, JMEAngleFunctions.getYAxisRotation(-1, 0));
				this.addEntityToRandomPosition(snowman);
				//Globals.p("Placed " + i + " snowmen.");
			}

			// Place trees
			int numTrees = mapSize/2;
			for (int i=0 ; i<numTrees ; i++) {
				SnowTree1 tree1 = new SnowTree1(this, getNextEntityID(), mapSize/2, 0, mapSize/2, JMEAngleFunctions.getYAxisRotation(-1, 0));
				this.addEntityToRandomPosition(tree1);
				//Globals.p("Placed " + i + " tree.");
			}
			for (int i=0 ; i<numTrees ; i++) {
				SnowTree2 tree1 = new SnowTree2(this, getNextEntityID(), mapSize/2, 0, mapSize/2, JMEAngleFunctions.getYAxisRotation(-1, 0));
				this.addEntityToRandomPosition(tree1);
				//Globals.p("Placed " + i + " tree.");
			}

			// Place igloos
			int numIgloos = mapSize/2;
			for (int i=0 ; i<numIgloos ; i++) {
				Igloo igloo = new Igloo(this, getNextEntityID(), mapSize/2, 0, mapSize/2, JMEAngleFunctions.getYAxisRotation(-1, 0));
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
		InvisibleMapBorder borderR = new InvisibleMapBorder(this, getNextEntityID(), mapSize+InvisibleMapBorder.BORDER_WIDTH, 0, 0, mapSize, Vector3f.UNIT_Z);
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
		
		if (Globals.TEST_BULLET_REWINDING) {
			MovingTargetSnowman snowman = new MovingTargetSnowman(this, getNextEntityID(), mapSize/2, 0.01f, mapSize/2, JMEAngleFunctions.getYAxisRotation(-1, 0));
			this.addEntityToRandomPosition(snowman);

		}
	}


	private void addEntityToRandomPosition(PhysicalEntity entity) {
		float x = NumberFunctions.rndFloat(2, mapSize-3);
		float z = NumberFunctions.rndFloat(2, mapSize-3);
		this.actuallyAddEntity(entity);
		long start = System.currentTimeMillis();
		while (entity.simpleRigidBody.checkForCollisions(false).size() > 0) {
			if (System.currentTimeMillis() - start > 5000) {
				throw new RuntimeException("Taking too long to place an entity");
			}
			x = NumberFunctions.rndFloat(2, mapSize-3);
			z = NumberFunctions.rndFloat(2, mapSize-3);
			entity.setWorldTranslation(x, z);
		}
		// randomly rotate
		JMEAngleFunctions.rotateToWorldDirectionYAxis(entity.getMainNode(), NumberFunctions.rnd(0,  359));
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		SnowmanServerAvatar avatar = new SnowmanServerAvatar(this, client, client.remoteInput, entityid);

		IAbility abilityGun = new SnowballLauncher(this, getNextEntityID(), client.getPlayerID(), avatar, entityid, (byte)0, client);
		this.actuallyAddEntity(abilityGun);

		return avatar;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		/*PhysicalEntity pea = a.userObject;
		//PhysicalEntity peb = b.userObject;

		/*if (pea instanceof SnowFloor == false && peb instanceof SnowFloor == false) {
			Globals.p("Collision between " + pea + " and " + peb);
		}*/

		super.collisionOccurred(a, b);

	}


	@Override
	protected byte getWinningSideAtEnd() {
		int highestScore = -1;
		byte winningSide = -1;
		boolean draw = false;
		for(ClientData c : this.clientList.getClients()) {
			UASimplePlayerData spd = (UASimplePlayerData)c.playerData;
			if (spd.score > highestScore) {
				winningSide = spd.side;
				highestScore = spd.score;
				draw = false;
			} else if (spd.score == highestScore) {
				draw = true;
			}
		}
		if (draw) {
			return -1;
		}
		return winningSide;
	}


	@Override
	protected Class[] getListofMessageClasses() {
		return new Class[] {UASimplePlayerData.class};
	}


	@Override
	public byte getSide(ClientData client) {
		return (byte) client.id; // Everyone is on a different side.  Todo - check  > 127
	}


	@Override
	public boolean doWeHaveSpaces() {
		return true; // Always!
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return collisionValidator.canCollide(a, b);
	}


	@Override
	public int getMinPlayersRequiredForGame() {
		return 2;
	}


	@Override
	protected SimplePlayerData createSimplePlayerData() {
		return new UASimplePlayerData();
	}


}

