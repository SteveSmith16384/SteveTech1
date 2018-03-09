package com.scs.moonbaseassault.client;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.scs.moonbaseassault.MoonbaseAssaultStaticData;
import com.scs.moonbaseassault.client.hud.MoonbaseAssaultHUD;
import com.scs.moonbaseassault.netmessages.HudDataMessage;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.AbstractHUDImage;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.jme.JMEFunctions;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.server.Globals;

import ssmith.util.MyProperties;

public class MoonbaseAssaultClient extends AbstractGameClient {

	private MoonbaseAssaultClientEntityCreator entityCreator = new MoonbaseAssaultClientEntityCreator();
	private DirectionalLight sun;
	private AbstractHUDImage currentHUDTextImage;
	private MoonbaseAssaultHUD hud;
	
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
			String lobbyIpAddress = props.getPropertyAsString("lobbyIpAddress", "localhost");
			int lobbyPort = props.getPropertyAsInt("lobbyPort", 6146);

			int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);
			
			float gravity = props.getPropertyAsFloat("gravity", -5f);
			float aerodynamicness = props.getPropertyAsFloat("aerodynamicness", 0.99f);

			float mouseSensitivity = props.getPropertyAsFloat("mouseSensitivity", 1f);

			new MoonbaseAssaultClient(gameIpAddress, gamePort, lobbyIpAddress, lobbyPort,
					tickrateMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness,
					mouseSensitivity);
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}
	}


	public MoonbaseAssaultClient(String gameIpAddress, int gamePort, String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int clientRenderDelayMillis, int timeoutMillis, float gravity, float aerodynamicness,
			float mouseSensitivity) {
		super(MoonbaseAssaultStaticData.NAME, null, gameIpAddress, gamePort, lobbyIpAddress, lobbyPort, 
				tickrateMillis, clientRenderDelayMillis, timeoutMillis, gravity, aerodynamicness, mouseSensitivity);
	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		this.getViewPort().setBackgroundColor(ColorRGBA.Black);

		//getGameNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));

		// Add shadows
		final int SHADOWMAP_SIZE = 1024*2;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(getAssetManager(), SHADOWMAP_SIZE, 2);
		dlsr.setLight(sun);
		this.viewPort.addProcessor(dlsr);

	}


	@Override
	protected void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(.6f));
		getGameNode().addLight(al);

		sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(.5f, -1f, .5f).normalizeLocal());
		getGameNode().addLight(sun);
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		super.simpleUpdate(tpf_secs);
	}


	@Override
	protected void handleMessage(MyAbstractMessage message) {
		if (message instanceof HudDataMessage) {
			HudDataMessage hdm = (HudDataMessage) message;
			// todo
		} else {
			super.handleMessage(message);
		}
	}
	

	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b, Vector3f point) {
		//PhysicalEntity pea = a.userObject;
		//PhysicalEntity peb = b.userObject;

		super.collisionOccurred(a, b, point);

	}


	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityMessage msg) {
		return entityCreator.createEntity(client, msg);
	}


	@Override
	protected void playerHasWon() {
		removeCurrentHUDTextImage();
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/victory.png", this.cam.getWidth()/2, this.cam.getHeight()/2, 5);
	}


	@Override
	protected void playerHasLost() {
		removeCurrentHUDTextImage();
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", this.cam.getWidth()/2, this.cam.getHeight()/2, 5);
	}


	@Override
	protected void gameIsDrawn() {
		removeCurrentHUDTextImage();
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", this.cam.getWidth()/2, this.cam.getHeight()/2, 5);
	}


	@Override
	protected IHUD createHUD() {
		hud = new MoonbaseAssaultHUD(this, this.getCamera());
		return hud;
	}


	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		switch (newStatus) {
		case SimpleGameData.ST_WAITING_FOR_PLAYERS:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/waitingforplayers.png", width, height, 5);
			break;
		case SimpleGameData.ST_DEPLOYING:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/getready.png", width, height, 5);
			break;
		case SimpleGameData.ST_STARTED:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/missionstarted.png", width, height, 5);
			break;
		case SimpleGameData.ST_FINISHED:
			// Don't show anything, this will be handled with a win/lose message
			break;
		default:
			// DO nothing
		}

	}

	
	private void removeCurrentHUDTextImage() {
		if (this.currentHUDTextImage != null) {
			if (currentHUDTextImage.getParent() != null) {
				currentHUDTextImage.remove();
			}
		}
	}


	@Override
	protected Spatial getPlayersWeaponModel() {
		Spatial model = assetManager.loadModel("Models/pistol/pistol.blend");
		JMEFunctions.setTextureOnSpatial(assetManager, model, "Models/pistol/pistol_tex.png");
		model.scale(0.2f);
		model.setLocalTranslation(0f, 0f, -3f);
		return model;
	}


	@Override
	protected Class[] getListofMessageClasses() {
		return new Class[] {HudDataMessage.class};
	}

}
