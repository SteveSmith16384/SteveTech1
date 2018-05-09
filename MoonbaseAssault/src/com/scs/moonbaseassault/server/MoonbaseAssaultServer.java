package com.scs.moonbaseassault.server;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jme3.system.JmeContext;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.entities.GasCannister;
import com.scs.moonbaseassault.entities.MA_AISoldier;
import com.scs.moonbaseassault.entities.SoldierServerAvatar;
import com.scs.moonbaseassault.netmessages.HudDataMessage;
import com.scs.moonbaseassault.shared.MoonbaseAssaultCollisionValidator;
import com.scs.moonbaseassault.shared.MoonbaseAssaultGameData;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

import ssmith.astar.IAStarMapInterface;
import ssmith.lang.NumberFunctions;
import ssmith.util.MyProperties;

public class MoonbaseAssaultServer extends AbstractGameServer implements IAStarMapInterface {

	public static final String GAME_ID = "Moonbase Assault";

	public static final float CEILING_HEIGHT = 1.4f;

	private int scannerData[][];
	private List<Point> computerSquares;
	public ArrayList<Point>[] deploySquares;
	private MoonbaseAssaultCollisionValidator collisionValidator = new MoonbaseAssaultCollisionValidator();
	//public Node subNodeX0Y0, subNodeX1Y0, subNodeX0Y1, subNodeX1Y1, ceilingNode, floorNode; // todo - remove all this and do automatically


	public static void main(String[] args) {
		try {
			MyProperties props = null;
			if (args.length > 0) {
				props = new MyProperties(args[0]);
			} else {
				props = new MyProperties();
				Globals.p("No config file specified.  Using defaults.");
			}
			String gameIpAddress = props.getPropertyAsString("gameIpAddress", "localhost");
			int gamePort = props.getPropertyAsInt("gamePort", 6145);
			//String lobbyIpAddress = null;//props.getPropertyAsString("lobbyIpAddress", "localhost");
			//int lobbyPort = props.getPropertyAsInt("lobbyPort", 6146);

			int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			int sendUpdateIntervalMillis = props.getPropertyAsInt("sendUpdateIntervalMillis", 40);
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);
			//float gravity = props.getPropertyAsFloat("gravity", -5);
			//float aerodynamicness = props.getPropertyAsFloat("aerodynamicness", 0.99f);

			//startLobbyServer(lobbyPort, timeoutMillis); // Start the lobby in the same process, why not?  Feel from to comment this line out and run it seperately (If you want a lobby).

			new MoonbaseAssaultServer(gameIpAddress, gamePort, //lobbyIpAddress, lobbyPort, 
					tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis);//, gravity, aerodynamicness);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	private static void startLobbyServer(int lobbyPort, int timeout) {
		Thread r = new Thread("LobbyServer") {

			@Override
			public void run() {
				try {
					new MoonbaseAssaultLobbyServer(lobbyPort, timeout);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		r.start();


	}
	 */

	private MoonbaseAssaultServer(String gameIpAddress, int gamePort, //String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int sendUpdateIntervalMillis, int clientRenderDelayMillis, int timeoutMillis) throws IOException {
		super(GAME_ID, new GameOptions(5*1000, 10*60*1000, 10*1000, 
				gameIpAddress, gamePort, //lobbyIpAddress, lobbyPort, 
				10, 5), tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis);//, gravity, aerodynamicness);
		start(JmeContext.Type.Headless);


	}


	@Override
	public void simpleInitApp() {
		/*subNodeX0Y0 = new Node("00");
		subNodeX1Y0 = new Node("10");
		subNodeX0Y1 = new Node("01");
		subNodeX1Y1 = new Node("11");
		ceilingNode = new Node("Ceiling");
		floorNode = new Node("Floor");*/

		super.gameData = new MoonbaseAssaultGameData(this.getGameID()); // Replace normal data

		super.simpleInitApp();
	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		float startHeight = .1f;
		List<Point> deploySquares = this.deploySquares[avatar.side-1];
		boolean found = false;
		for (Point p : deploySquares) {
			avatar.setWorldTranslation(p.x+0.5f, startHeight, p.y+0.5f);
			if (avatar.simpleRigidBody.checkForCollisions().size() == 0) {
				found = true;
				break;
			}
		}
		if (found) {
			Globals.p("Player starting at " + avatar.getWorldTranslation());
		} else {
			throw new RuntimeException("No space to start!");
		}
	}


	public MoonbaseAssaultGameData getMAGameData() {
		return (MoonbaseAssaultGameData)super.gameData;
	}


	@Override
	protected void createGame() {
		/*this.getGameNode().attachChild(subNodeX0Y0);
		this.getGameNode().attachChild(subNodeX1Y0);
		this.getGameNode().attachChild(subNodeX0Y1);
		this.getGameNode().attachChild(subNodeX1Y1);
		this.getGameNode().attachChild(this.ceilingNode);*/

		MapLoader map = new MapLoader(this);
		try {
			//map.loadMap("/serverdata/moonbaseassault_small.csv");
			map.loadMap("/serverdata/moonbaseassault.csv");
			scannerData = map.scannerData;
			this.deploySquares = map.deploySquares;

			this.computerSquares = new ArrayList<Point>();
			for (int y=0 ; y<scannerData.length ; y++) {
				for (int x=0 ; x<scannerData.length ; x++) {
					if (this.scannerData[x][y] == MapLoader.COMPUTER) {
						computerSquares.add(new Point(x, y));
					}
				}
			}

			//Spaceship1 ss = new Spaceship1(this, this.getNextEntityID(), 8, 0f, 8, JMEAngleFunctions.getRotation(-1, 0));
			//todo - re-add this.actuallyAddEntity(ss);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		/*
		// Testing
		SlidingDoor door = new SlidingDoor(this, getNextEntityID(), 2, 0, 2, 1, CEILING_HEIGHT, "Textures/door_lr.png", 0);
		this.actuallyAddEntity(door);

		//SlidingDoor doorUD = new SlidingDoor(this, getNextEntityID(), 3, 0, 3, 1, CEILING_HEIGHT, "Textures/door_lr.png", 270);
		//this.actuallyAddEntity(doorUD);

		/*

		//MoonbaseWall wall = new MoonbaseWall(this, getNextEntityID(), 0, 0, 0, 1, CEILING_HEIGHT, 1, "Textures/spacewall2.png");//, 270);
		//this.actuallyAddEntity(wall);

		//MoonbaseWall wall2 = new MoonbaseWall(this, getNextEntityID(), 4, 0, 4, 3, CEILING_HEIGHT, 1, "Textures/spacewall2.png");//, 0);
		//this.actuallyAddEntity(wall2);

		MoonbaseWall wall3 = new MoonbaseWall(this, getNextEntityID(), 6, 0, 4, 1, CEILING_HEIGHT, 3, "Textures/spacewall2.png");//, 0);
		this.actuallyAddEntity(wall3);
		 */

		// Place floor & ceiling last
		//Floor floor = new Floor(this, getNextEntityID(), 0, 0, 0, mapSize, .5f, mapSize, "Textures/escape_hatch.jpg");
		//this.actuallyAddEntity(floor);

		GasCannister gas = new GasCannister(this, getNextEntityID(), 2f, 0.5f, 2f);
		this.actuallyAddEntity(gas);

		// Add AI soldiers
		for (int side=1 ; side<=2 ; side++) {
			for (int i=0 ; i<3 ; i++) {
				String name = (side == 1 ? "Attacker" : "Defender") + " " + (i+1);
				MA_AISoldier s = new MA_AISoldier(this, this.getNextEntityID(), 0,0,0, side, AbstractAvatar.ANIM_IDLE, name);
				this.actuallyAddEntity(s);
				moveAISoldierToStartPosition(s, s.side);
			}
		}

	}


	private void moveAISoldierToStartPosition(PhysicalEntity soldier, int side) {
		float startHeight = .1f;
		List<Point> deploySquares = this.deploySquares[side-1];
		boolean found = false;
		while (true) { // todo - only try a certain number of times
			Point p = deploySquares.get(NumberFunctions.rnd(0, deploySquares.size()-1));
			//for (Point p : deploySquares) {
			soldier.setWorldTranslation(p.x+0.5f, startHeight, p.y+0.5f);
			if (soldier.simpleRigidBody.checkForCollisions().size() == 0) {
				found = true;
				break;
			}
		}
		if (found) {
			Globals.p("AISoldier starting at " + soldier.getWorldTranslation());
		} else {
			throw new RuntimeException("No space to start!");
		}
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		SoldierServerAvatar avatar = new SoldierServerAvatar(this, client, client.remoteInput, entityid);
		return avatar;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		if (pa.type != MoonbaseAssaultClientEntityCreator.FLOOR && pb.type != MoonbaseAssaultClientEntityCreator.FLOOR) {
			//Globals.p("Collision between " + pa + " and " + pb);
		}

		super.collisionOccurred(a, b);

	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return this.collisionValidator.canCollide(a, b);
	}


	@Override
	protected void playerJoinedGame(ClientData client) {
		this.gameNetworkServer.sendMessageToClient(client, new HudDataMessage(this.scannerData));
	}


	@Override
	protected Class[] getListofMessageClasses() {
		return new Class[] {HudDataMessage.class, MoonbaseAssaultGameData.class};
	}


	@Override
	public int getSide(ClientData client) {
		// Players always on side 1?
		return 1;
	}


	private HashMap<Integer, Integer> getPlayersPerSide() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		// Load with empty side data
		for (int side=1 ; side<=2 ; side++) {
			map.put(side,  0);
		}


		for (ClientData client : this.clients.values()) {
			if (client.avatar != null) {
				if (!map.containsKey(client.side)) {
					map.put(client.side, 0);
				}
				int val = map.get(client.side);
				val++;
				map.put(client.side, val);
			}
		}
		return map;
	}


	@Override
	public boolean doWeHaveSpaces() {
		int currentPlayers = 0;
		for(ClientData c : this.clients.values()) {
			if (c.clientStatus == ClientData.ClientStatus.Accepted) {  // only count players actually Accepted!
				currentPlayers++;
			}
		}
		return currentPlayers < 12; // 2 sides, 6 per side
	}


	public List<Point> getComputerSquares() {
		return this.computerSquares;
	}


	public void computerDestroyed() {
		this.getMAGameData().pointsForSide[1] += 10;
		checkForWinner();
	}


	@Override
	public void playerKilled(AbstractServerAvatar avatar) {
		super.playerKilled(avatar);

		if (avatar.side == 1) {
			this.getMAGameData().pointsForSide[2] += 10;
			checkForWinner();
		}
	}


	private void checkForWinner() {
		if (this.getGameData().getGameStatus() == SimpleGameData.ST_STARTED) {
			if (this.getMAGameData().pointsForSide[1] >= 100 || this.getMAGameData().pointsForSide[2] >= 100) {
				this.gameStatusChanged(SimpleGameData.ST_FINISHED);
			}
		}
	}


	@Override
	protected int getWinningSide() {
		for (int s=1 ; s<=2 ; s++) {
			if (this.getMAGameData().pointsForSide[s] >= 100) {
				return s;
			}
		}
		return 2;
	}


	// AStar --------------------------------

	@Override
	public int getMapWidth() {
		return this.scannerData[0].length;
	}


	@Override
	public int getMapHeight() {
		return this.scannerData.length;
	}


	@Override
	public boolean isMapSquareTraversable(int x, int z) {
		return this.scannerData[x][z] != MapLoader.WALL && this.scannerData[x][z] != MapLoader.COMPUTER;
	}


	@Override
	public float getMapSquareDifficulty(int x, int z) {
		return 1;
	}

	//--------------------------------


	@Override
	public int getMinPlayersRequiredForGame() {
		return 1;
	}


}
