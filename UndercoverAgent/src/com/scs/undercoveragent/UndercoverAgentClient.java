package com.scs.undercoveragent;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.ValidateClientSettings;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.AbstractHUDImage;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;
import com.scs.undercoveragent.entities.SnowFloor;
import com.scs.undercoveragent.systems.client.FallingSnowflakeSystem;

import ssmith.util.MyProperties;
import ssmith.util.RealtimeInterval;

public class UndercoverAgentClient extends AbstractGameClient {

	private UndercoverAgentClientEntityCreator entityCreator;
	private FallingSnowflakeSystem snowflakeSystem;
	private AbstractHUDImage currentHUDImage;
	private DirectionalLight sun;
	private AbstractCollisionValidator collisionValidator;
	
	private UndercoverAgentHUD hud;
	private RealtimeInterval updateHudInterval = new RealtimeInterval(3000);
	private String ipAddress;
	private int port;
	private String playerName;

	public static void main(String[] args) {
		try {
			MyProperties props = null;
			if (args.length > 0) {
				props = new MyProperties(args[0]);
			} else {
				props = new MyProperties("playerConfig.txt");
				//Globals.p("Warning: No config file specified");
			}
			String gameIpAddress = props.getPropertyAsString("gameIpAddress", "localhost");//"192.168.1.217");
			int gamePort = props.getPropertyAsInt("gamePort", 6143);

			//int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			//int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			//int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);
			
			float mouseSensitivity = props.getPropertyAsFloat("mouseSensitivity", 1f);
			String name = props.getPropertyAsString("playerName", "");
			
			new UndercoverAgentClient(gameIpAddress, gamePort,
					Globals.DEFAULT_TICKRATE, Globals.DEFAULT_RENDER_DELAY, Globals.DEFAULT_NETWORK_TIMEOUT,
					mouseSensitivity, name);
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}
	}


	private UndercoverAgentClient(String gameIpAddress, int gamePort, 
			int tickrateMillis, int clientRenderDelayMillis, int timeoutMillis,
			float mouseSensitivity, String _playerName) {
		super(new ValidateClientSettings(UndercoverAgentServer.GAME_ID, "key", 1), UndercoverAgentServer.NAME, null, 
				tickrateMillis, clientRenderDelayMillis, timeoutMillis, mouseSensitivity);
		ipAddress = gameIpAddress;
		port = gamePort;
		playerName = _playerName;
		
		start();
		
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
		final int SHADOWMAP_SIZE = 512;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(getAssetManager(), SHADOWMAP_SIZE, 2);
		dlsr.setLight(sun);
		this.viewPort.addProcessor(dlsr);

		hud = new UndercoverAgentHUD(this, this.getCamera());
		this.getGuiNode().attachChild(hud);
		
		this.connect(ipAddress, port, false);
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

		if (this.joinedGame) {
			snowflakeSystem.process(tpf_secs);
		}
		
		hud.processByClient(this, tpf_secs);
		if (updateHudInterval.hitInterval()) {
			if (this.playersList != null) { // todo - move to HUD class
				for (SimplePlayerData spd : this.playersList) {
					if (spd.id == this.playerID) {
						UASimplePlayerData ua = (UASimplePlayerData)spd;
						this.hud.setScoreText(ua.score);
					}
				}
			}
		}
	}

	
	@Override
	protected String getPlayerName() {
		return playerName.length() > 0 ? this.playerName : super.getPlayerName();
	}

	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pea = a.userObject;
		PhysicalEntity peb = b.userObject;

		if (pea instanceof SnowFloor == false && peb instanceof SnowFloor == false) {
			//Globals.p("Collision between " + pea + " and " + peb);
		}

		super.collisionOccurred(a, b);

	}


	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg) {
		return entityCreator.createEntity(client, msg);
	}


	@Override
	protected void playerHasWon() {
		removeCurrentHUDImage();
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = this.cam.getHeight()/5;
		currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/victory.png", x, y, width, height, 5);
	}


	@Override
	protected void playerHasLost() {
		removeCurrentHUDImage();
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = this.cam.getHeight()/5;
		currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/defeat.png", x, y, width, height, 5);
	}


	@Override
	protected void gameIsDrawn() {
		removeCurrentHUDImage();
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = this.cam.getHeight()/5;
		currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/defeat.png", x, y, width, height, 5);
	}


	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		int width = this.cam.getWidth()/4;
		int height = this.cam.getHeight()/4;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = (int)(this.cam.getHeight() * 0.2f);
		switch (newStatus) {
		case SimpleGameData.ST_WAITING_FOR_PLAYERS:
			removeCurrentHUDImage();
			currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/waitingforplayers.png", x, y, width, height, 3);
			break;
		case SimpleGameData.ST_DEPLOYING:
			removeCurrentHUDImage();
			currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/getready.png", x, y, width, height, 3);
			break;
		case SimpleGameData.ST_STARTED:
			removeCurrentHUDImage();
			currentHUDImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/missionstarted.png", x, y, width, height, 3);
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
	protected Class[] getListofMessageClasses() {
		return new Class[] {UASimplePlayerData.class};
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return collisionValidator.canCollide(a, b);
	}
	
	
	@Override
	protected void showDamageBox() {
		this.hud.showDamageBox();
	}


	@Override
	protected void showMessage(String msg) {
		this.hud.appendToLog(msg);
	}


	@Override
	protected void appendToLog(String msg) {
		this.hud.appendToLog(msg);
	}


	@Override
	public void disconnected() {
		this.appendToLog("DISCONNECTED");
	}
	
}
