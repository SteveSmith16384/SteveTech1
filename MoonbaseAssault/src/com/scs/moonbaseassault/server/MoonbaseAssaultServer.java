package com.scs.moonbaseassault.server;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.entities.AISoldier;
import com.scs.moonbaseassault.entities.SoldierServerAvatar;
import com.scs.moonbaseassault.netmessages.HudDataMessage;
import com.scs.moonbaseassault.shared.MoonbaseAssaultCollisionValidator;
import com.scs.moonbaseassault.shared.MoonbaseAssaultGameData;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

import ssmith.astar.IAStarMapInterface;
import ssmith.util.MyProperties;

public class MoonbaseAssaultServer extends AbstractGameServer implements IAStarMapInterface {

	public static final String GAME_ID = "Moonbase Assault";

	public static final float CEILING_HEIGHT = 1.4f;

	private int scannerData[][];
	private List<Point> computerSquares;
	public ArrayList<Point>[] deploySquares;
	private MoonbaseAssaultCollisionValidator collisionValidator = new MoonbaseAssaultCollisionValidator();
	public Node subNodeX0Y0, subNodeX1Y0, subNodeX0Y1, subNodeX1Y1, ceilingNode, floorNode;


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
			int gamePort = props.getPropertyAsInt("gamePort", 6145);
			String lobbyIpAddress = null;//props.getPropertyAsString("lobbyIpAddress", "localhost");
			int lobbyPort = props.getPropertyAsInt("lobbyPort", 6146);

			int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			int sendUpdateIntervalMillis = props.getPropertyAsInt("sendUpdateIntervalMillis", 40);
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);
			float gravity = props.getPropertyAsFloat("gravity", -5);
			float aerodynamicness = props.getPropertyAsFloat("aerodynamicness", 0.99f);

			//startLobbyServer(lobbyPort, timeoutMillis); // Start the lobby in the same process, why not?  Feel from to comment this line out and run it seperately (If you want a lobby).

			new MoonbaseAssaultServer(gameIpAddress, gamePort, lobbyIpAddress, lobbyPort, 
					tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness);
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

	public MoonbaseAssaultServer(String gameIpAddress, int gamePort, String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int sendUpdateIntervalMillis, int clientRenderDelayMillis, int timeoutMillis, float gravity, float aerodynamicness) throws IOException {
		super(GAME_ID, new GameOptions("Moonbase Assault",  
				10*1000, 60*1000, 10*1000, 
				gameIpAddress, gamePort, lobbyIpAddress, lobbyPort, 
				10, 5), tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness);

	}


	@Override
	public void simpleInitApp() {
		subNodeX0Y0 = new Node("00");
		subNodeX1Y0 = new Node("10");
		subNodeX0Y1 = new Node("01");
		subNodeX1Y1 = new Node("11");
		ceilingNode = new Node("Ceiling");
		floorNode = new Node("Floor");

		super.simpleInitApp();
	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		float startHeight = .1f;
		List<Point> deploySquares = this.deploySquares[avatar.side-1];
		boolean found = false;
		for (Point p : deploySquares) {
			avatar.setWorldTranslation(new Vector3f(p.x+0.5f, startHeight, p.y+0.5f));
			if (avatar.simpleRigidBody.checkForCollisions() == null) {
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
		super.gameData = new MoonbaseAssaultGameData();
		
		this.getGameNode().attachChild(subNodeX0Y0);
		this.getGameNode().attachChild(subNodeX1Y0);
		this.getGameNode().attachChild(subNodeX0Y1);
		this.getGameNode().attachChild(subNodeX1Y1);
		this.getGameNode().attachChild(this.ceilingNode);

		MapLoader map = new MapLoader(this);
		try {
			map.loadMap("/serverdata/moonbaseassault_small.csv");
			scannerData = map.scannerData;
			this.deploySquares = map.deploySquares;

			int maxSoldiers = 0;

			this.computerSquares = new ArrayList<Point>();
			for (int y=0 ; y<scannerData.length ; y++) {
				for (int x=0 ; x<scannerData.length ; x++) {
					if (this.scannerData[x][y] == MapLoader.COMPUTER) {
						computerSquares.add(new Point(x, y));
					} else if (this.scannerData[x][y] == MapLoader.DOOR_LR) { // || this.scannerData[x][y] == MapLoader.DOOR_UD) {
						if (maxSoldiers > 0) {
							AISoldier s = new AISoldier(this, this.getNextEntityID(), x + 0.5f, .3f, y + 1.5f, 2);
							this.actuallyAddEntity(s);
							Globals.p("Adding soldier to " + x + ", " + y);
							maxSoldiers--;
						}
					}
				}
			}

			//AISoldier s = new AISoldier(this, this.getNextEntityID(), map.firstInteriorFloor.x + 0.5f, .3f, map.firstInteriorFloor.y + 0.5f, 2);
			//this.actuallyAddEntity(s);

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

		Computer computer = new Computer(this, getNextEntityID(), 5, 0, 5, 1f, 1f, 1f, "Textures/computerconsole2.jpg");
		this.actuallyAddEntity(computer);


		float mapSize = 20f;
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

	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		SoldierServerAvatar avatar = new SoldierServerAvatar(this, client, client.getPlayerID(), client.remoteInput, entityid);

		//IAbility abilityGun = new LaserRifle(this, getNextEntityID(), avatar, 0, client);
		//this.actuallyAddEntity(abilityGun);

		return avatar;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b, Vector3f point) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		if (pa.type != MoonbaseAssaultClientEntityCreator.FLOOR && pb.type != MoonbaseAssaultClientEntityCreator.FLOOR) {
			Globals.p("Collision between " + pa + " and " + pb);
		}

		super.collisionOccurred(a, b, point);

	}


	@Override
	public float getAvatarStartHealth(AbstractAvatar avatar) {
		return 100;
	}


	@Override
	protected int getWinningSide() {
		return 2;
	/*	int highestScore = -1;
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
		return winningSide;*/
	}

	
	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
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
		// todo - Check maxPlayersPerside, maxSides
		HashMap<Integer, Integer> map = getPlayersPerSide();
		// Get lowest amount
		int lowest = 999;
		int highest = -1;
		for (int i : map.values()) {
			if (i < lowest) {
				lowest = i;
			}
			if (i > highest) {
				highest = i;
			}
		}
		// Get the side
		Iterator<Integer> it = map.keySet().iterator();
		while (it.hasNext()) {
			int i = it.next();
			int val = map.get(i);
			if (val <= lowest) {
				return i;
			}
		}
		throw new RuntimeException("Should not get here");	
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
		// todo
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
}
