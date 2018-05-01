package twoweeks.server;

import java.io.IOException;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;
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
import twoweeks.entities.GenericStaticModel;
import twoweeks.entities.MercServerAvatar;
import twoweeks.entities.Terrain1;

public class TwoWeeksServer extends AbstractGameServer {

	public static final String GAME_ID = "Two Weeks";
	private static final Vector3f DOWN_VEC = new Vector3f(0, -1, 0);

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
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 1000000);
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

	private TwoWeeksServer(String gameIpAddress, int gamePort, //String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int sendUpdateIntervalMillis, int clientRenderDelayMillis, int timeoutMillis) throws IOException {
		super(GAME_ID, new GameOptions(10*1000, 60*1000, 10*1000, 
				gameIpAddress, gamePort, //lobbyIpAddress, lobbyPort, 
				10, 5), tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis);
		start(JmeContext.Type.Headless);

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

		Vector3f pos = this.getHeightAtPoint(x, z);// crs.getClosestCollision().getContactPoint();
		avatar.setWorldTranslation(x, pos.y + 50f, z);
	}


	public TwoWeeksGameData getMAGameData() {
		return (TwoWeeksGameData)super.gameData;
	}


	@Override
	protected void createGame() {
		super.gameData = new TwoWeeksGameData(nextGameID.getAndAdd(1));

		Terrain1 terrain = new Terrain1(this, getNextEntityID(), 0, 0, 0);
		this.actuallyAddEntity(terrain); // terrain.getMainNode().getWorldBound();

		Vector3f pos = null;
		/*
		Vector3f pos = this.getHeightAtPoint(90, 90);
		AISoldier s = new AISoldier(this, this.getNextEntityID(), pos.x, pos.y + 5, pos.z, 0);
		this.actuallyAddEntity(s);
		 */
		// Drop debug balls
		/*for (int z=80; z<=120 ; z+= 10) {
			for (int x=80; x<=120 ; x+= 10) {
				this.dropDebugSphere(terrain, x, z);
			}
		}*/

		// Place BigPalmTree
		pos = new Vector3f(95, 0, 85);
		GenericStaticModel tree = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/Desert/BigPalmTree.blend", 3f, "Models/Desert/Textures/PalmTree.png", pos.x, pos.y, pos.z, new Quaternion());
		pos.y = this.getLowestHeightAtPoint(tree.getMainNode());
		tree.setWorldTranslation(pos);
		this.actuallyAddEntity(tree); //tree.getMainNode().getWorldBound();
		tree = null;

		// Place BigTreeWithLeaves
		pos = new Vector3f(90, 0, 85);
		GenericStaticModel tree2 = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/BigTreeWithLeaves.blend", 3f, "Models/MoreNature/Blends/TreeTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		pos.y = this.getLowestHeightAtPoint(tree2.getMainNode());
		tree2.setWorldTranslation(pos);
		this.actuallyAddEntity(tree2); //tree.getMainNode().getWorldBound();
		tree2 = null;

		// SmallBush
		pos = new Vector3f(85, 0, 85);
		GenericStaticModel smallbush = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/SmallBush.blend", .5f, "Models/MoreNature/Blends/BushTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		pos.y = this.getLowestHeightAtPoint(smallbush.getMainNode());
		smallbush.setWorldTranslation(pos);
		this.actuallyAddEntity(smallbush); //tree.getMainNode().getWorldBound();
		smallbush = null;

		// Place SmallTreeWithLeave
		pos = new Vector3f(80, 0, 85);
		GenericStaticModel tree3 = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/SmallTreeWithLeave.blend", 3f, "Models/MoreNature/Blends/TreeTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		pos.y = this.getLowestHeightAtPoint(tree3.getMainNode());
		tree3.setWorldTranslation(pos);
		this.actuallyAddEntity(tree3); //tree.getMainNode().getWorldBound();
		tree3 = null;

		// Place TreeNoLeavesBig
		pos = new Vector3f(75, 0, 85);
		GenericStaticModel tree4 = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/TreeNoLeavesBig.blend", 3f, "Models/MoreNature/Blends/TreeTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		pos.y = this.getLowestHeightAtPoint(tree4.getMainNode());
		tree4.setWorldTranslation(pos);
		this.actuallyAddEntity(tree4); //tree.getMainNode().getWorldBound();
		tree4 = null;

		// Place TreeNoLeavesSmall
		pos = new Vector3f(70, 0, 85);
		GenericStaticModel tree5 = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/TreeNoLeavesSmall.blend", 3f, "Models/MoreNature/Blends/TreeTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		pos.y = this.getLowestHeightAtPoint(tree5.getMainNode());
		tree5.setWorldTranslation(pos);
		this.actuallyAddEntity(tree5); //tree.getMainNode().getWorldBound();
		tree5 = null;

		// Place trees
		for (int z=80; z<=120 ; z+= 10) {
			for (int x=80; x<=120 ; x+= 10) {
				GenericStaticModel tree6 = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/BigTreeWithLeaves.blend", 3f, "Models/MoreNature/Blends/TreeTexture.png", x, 0, z, new Quaternion());
				this.placeGenericModel(tree6, x, z);
			}
		}

		// todo
		// Place AI
		/*for (int z=80; z<=120 ; z+= 10) {
		for (int x=80; x<=120 ; x+= 10) {
		Vector3f pos = this.getHeightAtPoint(x, z);
		AISoldier s = new AISoldier(this, this.getNextEntityID(), pos.x, pos.y + 5, pos.z, 0);
		this.actuallyAddEntity(s);
		}
	}*/


		//DebuggingSphere ds = new DebuggingSphere(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.DEBUGGING_SPHERE, pos.x, pos.y, pos.z, true, false);
		//this.actuallyAddEntity(ds);

	}


	private void placeGenericModel(GenericStaticModel tree5, float x, float z) {
		Vector3f pos = new Vector3f(x, 0, z);
		pos.y = this.getLowestHeightAtPoint(tree5.getMainNode());
		tree5.setWorldTranslation(pos);
		this.actuallyAddEntity(tree5); //tree.getMainNode().getWorldBound();

	}


	private Vector3f getHeightAtPoint(float x, float z) {
		Ray r = new Ray(new Vector3f(x, 255, z), DOWN_VEC);
		CollisionResults crs = new CollisionResults();
		this.getGameNode().collideWith(r, crs);
		Vector3f pos = crs.getClosestCollision().getContactPoint();
		return pos;
	}


	private float getLowestHeightAtPoint(Spatial s) {
		CollisionResults crs = new CollisionResults();
		BoundingBox bb = (BoundingBox)s.getWorldBound();

		float res = 9999f;

		Ray r1 = new Ray(new Vector3f(bb.getCenter().x-bb.getXExtent(), 255, bb.getCenter().z-bb.getZExtent()), DOWN_VEC);
		crs.clear();
		this.getGameNode().collideWith(r1, crs);
		Vector3f pos1 = crs.getClosestCollision().getContactPoint();
		res = Math.min(res, pos1.y);

		Ray r2 = new Ray(new Vector3f(bb.getCenter().x+bb.getXExtent(), 255, bb.getCenter().z-bb.getZExtent()), DOWN_VEC);
		crs.clear();
		this.getGameNode().collideWith(r2, crs);
		Vector3f pos2 = crs.getClosestCollision().getContactPoint();
		res = Math.min(res, pos2.y);

		Ray r3 = new Ray(new Vector3f(bb.getCenter().x-bb.getXExtent(), 255, bb.getCenter().z+bb.getZExtent()), DOWN_VEC);
		crs.clear();
		this.getGameNode().collideWith(r3, crs);
		Vector3f pos3 = crs.getClosestCollision().getContactPoint();
		res = Math.min(res, pos3.y);

		Ray r4 = new Ray(new Vector3f(bb.getCenter().x+bb.getXExtent(), 255, bb.getCenter().z+bb.getZExtent()), DOWN_VEC);
		crs.clear();
		this.getGameNode().collideWith(r4, crs);
		Vector3f pos4 = crs.getClosestCollision().getContactPoint();
		res = Math.min(res, pos4.y);

		return res;
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


	@Override
	public int getMinPlayersRequiredForGame() {
		return 1;
	}


}
