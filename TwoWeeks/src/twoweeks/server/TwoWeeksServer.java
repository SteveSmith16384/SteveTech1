package twoweeks.server;

import java.io.IOException;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
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

		Ray r = new Ray(new Vector3f(x, 255, z), new Vector3f(0, -1, 0));
		CollisionResults crs = new CollisionResults();
		this.getGameNode().collideWith(r, crs);
		Vector3f pos = crs.getClosestCollision().getContactPoint();

		avatar.setWorldTranslation(x, pos.y + 10f, z);
	}


	public TwoWeeksGameData getMAGameData() {
		return (TwoWeeksGameData)super.gameData;
	}


	@Override
	protected void createGame() {
		super.gameData = new TwoWeeksGameData();

		Terrain1 terrain = new Terrain1(this, getNextEntityID(), 0, 0, 0);
		this.actuallyAddEntity(terrain);

		Vector3f pos = this.getHeightAtPoint(90, 90);
		AISoldier s = new AISoldier(this, this.getNextEntityID(), pos.x, pos.y + 5, pos.z, 0);
		this.actuallyAddEntity(s);

		// Drop debug balls
		/*for (int z=80; z<=120 ; z+= 10) {
			for (int x=80; x<=120 ; x+= 10) {
				this.dropDebugSphere(terrain, x, z);
			}
		}*/

		// Place tree
		pos = this.getHeightAtPoint(95, 85);
		GenericStaticModel tree = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/Desert/BigPalmTree.blend", 3f, "Models/Desert/Textures/PalmTree.png", pos.x, pos.y, pos.z, new Quaternion(), new Vector3f(1, -.1f, .5f));
		this.actuallyAddEntity(tree); //tree.getMainNode().getWorldBound();

		DebuggingSphere ds = new DebuggingSphere(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.DEBUGGING_SPHERE, pos.x, pos.y, pos.z, true, false);
		this.actuallyAddEntity(ds);

	}


	private Vector3f getHeightAtPoint(float x, float z) {
		Ray r = new Ray(new Vector3f(x, 255, z), new Vector3f(0, -1, 0));
		CollisionResults crs = new CollisionResults();
		this.getGameNode().collideWith(r, crs);
		Vector3f pos = crs.getClosestCollision().getContactPoint();
		return pos;
	}


	private float getHeightAtPoint(Spatial s) {
		CollisionResults crs = new CollisionResults();
		BoundingBox bb = (BoundingBox)s.getWorldBound();
		
		Ray r1 = new Ray(new Vector3f(bb.getCenter().x-bb.getXExtent(), 255, bb.getCenter().z-bb.getZExtent()), new Vector3f(0, -1, 0));
		crs.clear();
		this.getGameNode().collideWith(r1, crs);
		Vector3f pos1 = crs.getClosestCollision().getContactPoint();
		
		Ray r2 = new Ray(new Vector3f(bb.getCenter().x+bb.getXExtent(), 255, bb.getCenter().z-bb.getZExtent()), new Vector3f(0, -1, 0));
		crs.clear();
		this.getGameNode().collideWith(r2, crs);
		Vector3f pos2 = crs.getClosestCollision().getContactPoint();
		
		Ray r3 = new Ray(new Vector3f(bb.getCenter().x-bb.getXExtent(), 255, bb.getCenter().z+bb.getZExtent()), new Vector3f(0, -1, 0));
		crs.clear();
		this.getGameNode().collideWith(r3, crs);
		Vector3f pos3 = crs.getClosestCollision().getContactPoint();
		
		Ray r4 = new Ray(new Vector3f(bb.getCenter().x+bb.getXExtent(), 255, bb.getCenter().z+bb.getZExtent()), new Vector3f(0, -1, 0));
		crs.clear();
		this.getGameNode().collideWith(r4, crs);
		Vector3f pos4 = crs.getClosestCollision().getContactPoint();
		
		return pos;
	}


	private void dropDebugSphere(Terrain1 terrain, float x, float z) {
		Vector3f pos = this.getHeightAtPoint(x, z);// crs.getClosestCollision().getContactPoint();			
		DebuggingSphere ds = new DebuggingSphere(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.DEBUGGING_SPHERE, pos.x, pos.y, pos.z, true, false);
		this.actuallyAddEntity(ds);
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
		return 2; // todo
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
		return 1; // todo - return unique side

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
