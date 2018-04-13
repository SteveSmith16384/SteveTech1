package twoweeks.server;

import java.io.IOException;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

import ssmith.util.MyProperties;
import twoweeks.TwoWeeksCollisionValidator;
import twoweeks.TwoWeeksGameData;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.entities.AISoldier;
import twoweeks.entities.GenericStaticModel;
import twoweeks.entities.MercServerAvatar;
import twoweeks.entities.Terrain1;

public class TwoWeeksServer extends AbstractGameServer {

	public static final String GAME_ID = "Two Weeks";

	//public static final float MAP_SIZE = 100f;
	//public static final float CEILING_HEIGHT = 1.4f;

	private TwoWeeksCollisionValidator collisionValidator = new TwoWeeksCollisionValidator();


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

			new TwoWeeksServer(gameIpAddress, gamePort, //lobbyIpAddress, lobbyPort, 
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

	public TwoWeeksServer(String gameIpAddress, int gamePort, //String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int sendUpdateIntervalMillis, int clientRenderDelayMillis, int timeoutMillis) throws IOException {
		super(GAME_ID, new GameOptions(10*1000, 60*1000, 10*1000, 
				gameIpAddress, gamePort, //lobbyIpAddress, lobbyPort, 
				10, 5), tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis);//, gravity, aerodynamicness);

	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		super.simpleUpdate(tpf_secs);
	}
	
	
	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		float x = 100;//todo 10 + NumberFunctions.rndFloat(10, MAP_SIZE-20);
		float z = 100;//10 + NumberFunctions.rndFloat(10, MAP_SIZE-20);
		avatar.setWorldTranslation(x, 60, z);
	}


	public TwoWeeksGameData getMAGameData() {
		return (TwoWeeksGameData)super.gameData;
	}


	@Override
	protected void createGame() {
		super.gameData = new TwoWeeksGameData();

		// Place floor & ceiling last
		//Floor floor = new Floor(this, getNextEntityID(), "Floor", 0, 0, 0, MAP_SIZE, .5f, MAP_SIZE, "Textures/mud.png");
		//this.actuallyAddEntity(floor);
		
		Terrain1 terrain = new Terrain1(this, getNextEntityID(), 0, 0, 0);
		this.actuallyAddEntity(terrain);
		
		AISoldier s = new AISoldier(this, this.getNextEntityID(), 90, 60, 90, 0);
		this.actuallyAddEntity(s);
		
		// Drop debug balls
		for (int z=80; z<=120 ; z+= 10) {
			for (int x=80; x<=120 ; x+= 10) {
				this.dropDebugSphere(terrain, x, z);
			}
		}
		
		Ray r = new Ray(new Vector3f(95, 60, 95), new Vector3f(0, -1, 0));
		CollisionResults crs = new CollisionResults();
		terrain.getMainNode().collideWith(r, crs);
		if (crs.size() > 0) {
			Vector3f pos = crs.getClosestCollision().getContactPoint();
			//pos.y -= 1f;
			//GenericStaticModel tree = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/Desert/BigPalmTree.blend", 3f, "Models/Desert/Textures/PalmTree.png", pos.x, pos.y, pos.z, new Quaternion());
			//this.actuallyAddEntity(tree); //tree.getMainNode().getWorldBound();
		}

		/*
		Crate c = new Crate(this, getNextEntityID(), 1, 30, 1, 1, 1, 1f, "Textures/crate.png", 45);
		this.actuallyAddEntity(c);
		c = new Crate(this, getNextEntityID(), 1, 30, 6, 1, 1, 1f, "Textures/crate.png", 65);
		this.actuallyAddEntity(c);
		c = new Crate(this, getNextEntityID(), 6, 30, 1, 1, 1, 1f, "Textures/crate.png", 45);
		this.actuallyAddEntity(c);
		c = new Crate(this, getNextEntityID(), 6, 30, 6, 1, 1, 1f, "Textures/crate.png", 65);
		this.actuallyAddEntity(c);
		*/


	}


	private void dropDebugSphere(Terrain1 terrain, float x, float z) {
		Ray r = new Ray(new Vector3f(x, 60, z), new Vector3f(0, -1, 0));
		CollisionResults crs = new CollisionResults();
		terrain.getMainNode().collideWith(r, crs);
		if (crs.size() > 0) {
			Vector3f pos = crs.getClosestCollision().getContactPoint();			
			DebuggingSphere ds = new DebuggingSphere(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.DEBUGGING_SPHERE, pos.x, pos.y, pos.z, true, false);
			this.actuallyAddEntity(ds);
		}


	}
	
	
	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		MercServerAvatar avatar = new MercServerAvatar(this, client, client.getPlayerID(), client.remoteInput, entityid);

		return avatar;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		if (pa.type != TwoWeeksClientEntityCreator.TERRAIN1 && pb.type != TwoWeeksClientEntityCreator.TERRAIN1) {
			Globals.p("Collision between " + pa + " and " + pb);
		}

		super.collisionOccurred(a, b);

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
	}


	@Override
	protected Class[] getListofMessageClasses() {
		return new Class[] {TwoWeeksGameData.class};
	}


	@Override
	public int getSide(ClientData client) {
		// Players always on side 1?
		return 1;
		/*
		// This DOESN'T Check maxPlayersPerside, maxSides
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
		 */
	}


	@Override
	public boolean doWeHaveSpaces() {
		return true;
	}


	@Override
	public void playerKilled(AbstractServerAvatar avatar) {
		super.playerKilled(avatar);

		checkForWinner();
	}


	private void checkForWinner() {
		// todo
	}


}
