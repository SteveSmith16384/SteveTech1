package com.scs.undercoveragent;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.AbstractHUDImage;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.systems.client.FallingSnowflakeSystem;

import ssmith.util.MyProperties;

public class UndercoverAgentClient extends AbstractGameClient {

	private UndercoverAgentClientEntityCreator entityCreator;
	private FallingSnowflakeSystem snowflakeSystem;
	private AbstractHUDImage currentHUDImage;
	private DirectionalLight sun;
	private AbstractCollisionValidator collisionValidator;

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
			//String lobbyIpAddress = props.getPropertyAsString("lobbyIpAddress", "localhost");
			//int lobbyPort = props.getPropertyAsInt("lobbyPort", 6144);

			int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);
			
			float gravity = props.getPropertyAsFloat("gravity", -5f);
			float aerodynamicness = props.getPropertyAsFloat("aerodynamicness", 0.99f);

			float mouseSensitivity = props.getPropertyAsFloat("mouseSensitivity", 1f);

			new UndercoverAgentClient(gameIpAddress, gamePort, null, -1,
					tickrateMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness,
					mouseSensitivity);
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}
	}


	public UndercoverAgentClient(String gameIpAddress, int gamePort, String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int clientRenderDelayMillis, int timeoutMillis, float gravity, float aerodynamicness,
			float mouseSensitivity) {
		super(UndercoverAgentServer.GAME_ID, UndercoverAgentServer.NAME, null, gameIpAddress, gamePort, lobbyIpAddress, lobbyPort, 
				tickrateMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness, mouseSensitivity);
	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		entityCreator = new UndercoverAgentClientEntityCreator();
		collisionValidator = new AbstractCollisionValidator();
				
		this.getViewPort().setBackgroundColor(ColorRGBA.LightGray);

		getGameNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));

		this.snowflakeSystem = new FallingSnowflakeSystem(this);
		
		// Add shadows
		final int SHADOWMAP_SIZE = 1024;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(getAssetManager(), SHADOWMAP_SIZE, 2);
		dlsr.setLight(sun);
		this.viewPort.addProcessor(dlsr);

	}


	@Override
	protected void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(.3f));
		getGameNode().addLight(al);

		sun = new DirectionalLight();
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


	@Override
	protected void playerHasWon() {
		removeCurrentHUDImage();
		currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/victory.png", this.cam.getWidth()/2, this.cam.getHeight()/2, 5);
	}


	@Override
	protected void playerHasLost() {
		removeCurrentHUDImage();
		currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", this.cam.getWidth()/2, this.cam.getHeight()/2, 5);
	}


	@Override
	protected void gameIsDrawn() {
		removeCurrentHUDImage();
		currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", this.cam.getWidth()/2, this.cam.getHeight()/2, 5);
	}


	@Override
	protected IHUD createHUD() {
		return new UndercoverAgentHUD(this, this.getCamera());
	}


	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		switch (newStatus) {
		case SimpleGameData.ST_WAITING_FOR_PLAYERS:
			removeCurrentHUDImage();
			currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/waitingforplayers.png", width, height, 5);
			break;
		case SimpleGameData.ST_DEPLOYING:
			removeCurrentHUDImage();
			currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/getready.png", width, height, 5);
			break;
		case SimpleGameData.ST_STARTED:
			removeCurrentHUDImage();
			currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/missionstarted.png", width, height, 5);
			break;
		case SimpleGameData.ST_FINISHED:
			// Don't show anything, this will be handled with a win/lose message
			break;
		default:
			// DO nothing
		}

	}

	
	private void removeCurrentHUDImage() {
		if (this.currentHUDImage != null) {
			if (currentHUDImage.getParent() != null) {
				currentHUDImage.remove();
			}
		}
	}


	@Override
	protected Spatial getPlayersWeaponModel() {
		return null;
	}


	@Override
	protected Class[] getListofMessageClasses() {
		return null;
	}


	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		return collisionValidator.canCollide(a, b);
	}

}
