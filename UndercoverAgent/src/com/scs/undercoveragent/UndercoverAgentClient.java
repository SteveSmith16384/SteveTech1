package com.scs.undercoveragent;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.util.SkyFactory;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.server.Globals;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.systems.client.FallingSnowflakeSystem;

import ssmith.util.MyProperties;

public class UndercoverAgentClient extends AbstractGameClient {

	private UndercoverAgentClientEntityCreator entityCreator = new UndercoverAgentClientEntityCreator();
	private FallingSnowflakeSystem snowflakeSystem;
	
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
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);
			float gravity = props.getPropertyAsFloat("gravity", -5);
			float aerodynamicness = props.getPropertyAsFloat("aerodynamicness", 0.99f);
			
			new UndercoverAgentClient(gameIpAddress, gamePort, lobbyIpAddress, lobbyPort,
					tickrateMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness);
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}
	}


	public UndercoverAgentClient(String gameIpAddress, int gamePort, String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int clientRenderDelayMillis, int timeoutMillis, float gravity, float aerodynamicness) {
		super(UndercoverAgentStaticData.NAME, null, gameIpAddress, gamePort, lobbyIpAddress, lobbyPort, 
				tickrateMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness);
	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		this.getViewPort().setBackgroundColor(ColorRGBA.LightGray);

		getGameNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
		
		this.snowflakeSystem = new FallingSnowflakeSystem(this);
	}


	@Override
	protected void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(.3f));
		getGameNode().addLight(al);

		DirectionalLight sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(.5f, -1f, .5f).normalizeLocal());
		getGameNode().addLight(sun);
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		super.simpleUpdate(tpf_secs);
		
		if (this.clientStatus == AbstractGameClient.STATUS_STARTED) {
			snowflakeSystem.process(tpf_secs);
		}
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
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityMessage msg) {
		return entityCreator.createEntity(client, msg);
	}


}
