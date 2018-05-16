package twoweeks.server;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;
import ssmith.util.MyProperties;
import twoweeks.TwoWeeksCollisionValidator;
import twoweeks.TwoWeeksGameData;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.entities.GenericStaticModel;
import twoweeks.entities.MercServerAvatar;
import twoweeks.entities.TWIB_AISoldier;
import twoweeks.entities.Terrain1;

public class TwoWeeksServer extends AbstractGameServer implements ITerrainHeightAdjuster {

	private static final int MAP_SIZE = 200; // Size to use
	private static final int CITY_X = 20;
	private static final int CITY_Z = 20;
	private static final int CITY_SIZE = 60;
	private static final float SPACE_BETWEEN_BUILDINGS = 6f;
	public static final float LASER_DIAM = 0.01f;
	public static final float LASER_LENGTH = 0.5f;

	private static AtomicInteger nextSideNum = new AtomicInteger(1);

	public static final String GAME_ID = "Two Weeks";
	private static final Vector3f DOWN_VEC = new Vector3f(0, -1, 0);

	private Terrain1 terrain;
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
		super(GAME_ID, new GameOptions(10*1000, 10*60*1000, 10*1000, 
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
		float x = 10 + NumberFunctions.rndFloat(10, MAP_SIZE-20);
		float z = 10 + NumberFunctions.rndFloat(10, MAP_SIZE-20);

		Vector3f pos = this.getHeightAtPoint(x, z);
		avatar.setWorldTranslation(x, pos.y + 50f, z);
	}


	public TwoWeeksGameData getMAGameData() {
		return (TwoWeeksGameData)super.gameData;
	}


	@Override
	protected void createGame() {
		//super.gameData = new TwoWeeksGameData(nextGameID.getAndAdd(1));

		terrain = new Terrain1(this, getNextEntityID(), 0, 0, 0, this);
		this.actuallyAddEntity(terrain); // terrain.getMainNode().getWorldBound();
		// 1280 x 1280

		placeCity(CITY_X, CITY_Z);
		//placeCity(CITY_X+(CITY_SIZE*2), CITY_Z+(CITY_SIZE*2));

		{
			// Place trees
			/*for (int z=80; z<=120 ; z+= 10) {
				for (int x=80; x<=120 ; x+= 10) {
					GenericStaticModel tree6 = new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Tree", "Models/MoreNature/Blends/BigTreeWithLeaves.blend", 3f, "Models/MoreNature/Blends/TreeTexture.png", x, 0, z, new Quaternion());
					this.placeGenericModel(tree6, x, z);
				}
			}*/
			for (int num=0 ; num<10 ; num++) {
				//todo
			}
		}

		{
			// Place AI
			/*for (int num=0 ; num<10 ; num++) {
				Vector3f pos = new Vector3f(NumberFunctions.rndFloat(10, MAP_SIZE-10), 255, NumberFunctions.rndFloat(10, MAP_SIZE-10));
				TWIB_AISoldier s = new TWIB_AISoldier(this, this.getNextEntityID(), pos.x, pos.y + 5, pos.z, this.nextSideNum.getAndAdd(1), AbstractAvatar.ANIM_IDLE, "Enemy " + (num+1));
				this.actuallyAddEntity(s);
			}*/
		}

	}


	private void placeGenericModel(GenericStaticModel tree5, float x, float z) {
		Vector3f pos = new Vector3f(x, 0, z);
		pos.y = this.getLowestHeightAtPoint(tree5.getMainNode());
		tree5.setWorldTranslation(pos);
		this.actuallyAddEntity(tree5); //tree5.getMainNode().getWorldBound();

	}


	private void placeCity(int sx, int sz) {
		// Add buildings
		for (int z=sz ; z<sz+CITY_SIZE ; z+=SPACE_BETWEEN_BUILDINGS) {
			for (int x=sx ; x<sx+CITY_SIZE ; x+=SPACE_BETWEEN_BUILDINGS) {
				if (NumberFunctions.rnd(1, 3) == 1) {
					Vector3f pos = new Vector3f(x, 0f, z);
					GenericStaticModel building = this.getRandomBuilding(pos);// new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "BurgerShop", "Models/Suburban pack Vol.2 by Quaternius/Blends/BurgerShop.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/BurgerShopTexture.png", pos.x, pos.y, pos.z, new Quaternion());
					pos.y = this.getLowestHeightAtPoint(building.getMainNode());
					building.setWorldTranslation(pos);
					this.actuallyAddEntity(building); //building.getMainNode().getWorldBound();
					//Globals.p("Placed building at " + pos);
				}
			}
		}

		// Add cars
		for (int z=sz+3 ; z<sz+CITY_SIZE ; z+=SPACE_BETWEEN_BUILDINGS) {
			for (int x=sx+3 ; x<sx+CITY_SIZE ; x+=SPACE_BETWEEN_BUILDINGS) {
				if (NumberFunctions.rnd(1, 3) == 1) {
					Vector3f pos = new Vector3f(x, 0f, z);
					GenericStaticModel vehicle = this.getRandomVehicle(pos);
					pos.y = this.getLowestHeightAtPoint(vehicle.getMainNode());
					vehicle.setWorldTranslation(pos);
					this.actuallyAddEntity(vehicle); //building.getMainNode().getWorldBound();
					Globals.p("Placed vehicle at " + pos);
				}
			}
		}


	}


	private GenericStaticModel getRandomVehicle(Vector3f pos) {
		float height = 0.7f;
		int i = NumberFunctions.rnd(1, 4);
		switch (i) {
		case 1:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "BasicCar", "Models/Car pack by Quaternius/BasicCar.blend", height, "Models/Car pack by Quaternius/CarTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		case 2:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "CopCar", "Models/Car pack by Quaternius/CopCar.blend", height, "Models/Car pack by Quaternius/CopTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		case 3:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "RaceCar", "Models/Car pack by Quaternius/RaceCar.blend", height, "Models/Car pack by Quaternius/RaceCarTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		case 4:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Taxi", "Models/Car pack by Quaternius/Taxi.blend", height, "Models/Car pack by Quaternius/TaxiTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		default:
			throw new RuntimeException("Invalid number: " + i);
		}

	}


	private GenericStaticModel getRandomBuilding(Vector3f pos) {
		int i = NumberFunctions.rnd(1, 5);
		switch (i) {
		case 1:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "BigBuilding", "Models/Suburban pack Vol.2 by Quaternius/Blends/BigBuilding.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/BigBuildingTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		case 2:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "BurgerShop", "Models/Suburban pack Vol.2 by Quaternius/Blends/BurgerShop.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/BurgerShopTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		case 3:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "Shop", "Models/Suburban pack Vol.2 by Quaternius/Blends/Shop.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/ShopTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		case 4:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "SimpleHouse", "Models/Suburban pack Vol.2 by Quaternius/Blends/SimpleHouse.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/HouseTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		case 5:
			return new GenericStaticModel(this, this.getNextEntityID(), TwoWeeksClientEntityCreator.GENERIC_STATIC_MODEL, "SimpleHouse2", "Models/Suburban pack Vol.2 by Quaternius/Blends/SimpleHouse2.blend", -1, "Models/Suburban pack Vol.2 by Quaternius/Blends/Textures/HouseTexture.png", pos.x, pos.y, pos.z, new Quaternion());
		default:
			throw new RuntimeException("Invalid number: " + i);
		}

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
		DebuggingSphere ds = new DebuggingSphere(this, this.getNextEntityID(), pos.x, pos.y, pos.z, true, false);
		this.actuallyAddEntity(ds);
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		MercServerAvatar avatar = new MercServerAvatar(this, client, client.remoteInput, entityid);
		return avatar;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		if (pa.type != TwoWeeksClientEntityCreator.TERRAIN1 && pb.type != TwoWeeksClientEntityCreator.TERRAIN1) {
			//Globals.p("Collision between " + pa + " and " + pb);
		}

		super.collisionOccurred(a, b);

	}


	@Override
	protected int getWinningSideAtEnd() {
		return 2; // todo
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
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
		return nextSideNum.getAndAdd(1);

	}


	@Override
	public boolean doWeHaveSpaces() {
		return true; // todo - not if game started?
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


	@Override
	public void adjustHeight(AbstractHeightMap heightmap) {
		float h = heightmap.getInterpolatedHeight(CITY_X, CITY_Z); 
		for (int z=CITY_Z ; z<CITY_Z+CITY_SIZE ; z++) {
			for (int x=CITY_X ; x<CITY_X+CITY_SIZE ; x++) {
				//Globals.p("x=" + x + ", z=" + z);
				heightmap.setHeightAtPoint(h, x, z);
			}			
		}

	}


}
