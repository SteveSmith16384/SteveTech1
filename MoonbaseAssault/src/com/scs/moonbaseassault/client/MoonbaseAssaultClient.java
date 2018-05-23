package com.scs.moonbaseassault.client;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.scs.moonbaseassault.client.hud.MoonbaseAssaultHUD;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.MA_AISoldier;
import com.scs.moonbaseassault.netmessages.HudDataMessage;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.moonbaseassault.shared.MoonbaseAssaultCollisionValidator;
import com.scs.moonbaseassault.shared.MoonbaseAssaultGameData;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.AbstractHUDImage;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;

import ssmith.util.MyProperties;
import ssmith.util.RealtimeInterval;

public class MoonbaseAssaultClient extends AbstractGameClient {

	private MoonbaseAssaultClientEntityCreator entityCreator;
	private DirectionalLight sun;
	private AbstractHUDImage currentHUDTextImage;
	private MoonbaseAssaultHUD hud;
	private RealtimeInterval updateHUDInterval;
	private MoonbaseAssaultCollisionValidator collisionValidator;

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

			//float gravity = props.getPropertyAsFloat("gravity", -5f);
			//float aerodynamicness = props.getPropertyAsFloat("aerodynamicness", 0.99f);

			float mouseSensitivity = props.getPropertyAsFloat("mouseSensitivity", 1f);

			new MoonbaseAssaultClient(gameIpAddress, gamePort, lobbyIpAddress, lobbyPort,
					tickrateMillis, clientRenderDelayMillis, timeoutMillis, //gravity, aerodynamicness,
					mouseSensitivity);
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}
	}


	private MoonbaseAssaultClient(String gameIpAddress, int gamePort, String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int clientRenderDelayMillis, int timeoutMillis,// float gravity, float aerodynamicness,
			float mouseSensitivity) {
		super(MoonbaseAssaultServer.GAME_ID, "Moonbase Assault", null, gameIpAddress, gamePort, //lobbyIpAddress, lobbyPort, 
				tickrateMillis, clientRenderDelayMillis, timeoutMillis, mouseSensitivity); // gravity, aerodynamicness, 

		start();
	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		entityCreator = new MoonbaseAssaultClientEntityCreator();
		collisionValidator = new MoonbaseAssaultCollisionValidator();

		this.getViewPort().setBackgroundColor(ColorRGBA.Black);

		//getGameNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));

		// Add shadows
		final int SHADOWMAP_SIZE = 1024*2;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(getAssetManager(), SHADOWMAP_SIZE, 2);
		dlsr.setLight(sun);
		this.viewPort.addProcessor(dlsr);

		updateHUDInterval = new RealtimeInterval(2000);

	}


	@Override
	protected void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(.6f));
		getGameNode().addLight(al);

		sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(.4f, -.8f, .4f).normalizeLocal());
		getGameNode().addLight(sun);
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		super.simpleUpdate(tpf_secs);

		if (this.updateHUDInterval.hitInterval()) {
			// Get data for HUD
			List<Point> units = new LinkedList<Point>();
			List<Point> computers = new LinkedList<Point>();
			for (IEntity e : this.entities.values()) {
				if (e instanceof PhysicalEntity) {
					PhysicalEntity pe = (PhysicalEntity)e;  //pe.getWorldRotation();
					if (pe instanceof Computer) {
						Vector3f pos = pe.getWorldTranslation();
						computers.add(new Point((int)pos.x, (int)pos.z));
					} else if (pe instanceof MA_AISoldier) {
						MA_AISoldier ai = (MA_AISoldier)pe;
						if (ai.getSide() == 1) { // Only show attackers
							Vector3f pos = pe.getWorldTranslation();
							units.add(new Point((int)pos.x, (int)pos.z));
						}
					}
				}
			}
			Point player = null;
			if (currentAvatar != null) {
				Vector3f v = this.currentAvatar.getWorldTranslation();
				player = new Point((int)v.x, (int)v.z);
			}
			//this.hud.hudMapImage.mapImageTex.setOtherData(player, units, computers);
			this.hud.setOtherData(player, units, computers);
		}
	}


	@Override
	protected void handleMessage(MyAbstractMessage message) {
		if (message instanceof HudDataMessage) {
			HudDataMessage hdm = (HudDataMessage) message;
			this.hud.setMapData(hdm.scannerData);
			this.hud.setCompsDestroyed(hdm.compsDestroyed);
		} else {
			super.handleMessage(message);
		}
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return this.collisionValidator.canCollide(a, b);
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		super.collisionOccurred(a, b);

	}


	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg) {
		return entityCreator.createEntity(client, msg);
	}


	@Override
	protected void playerHasWon() {
		removeCurrentHUDTextImage();
		int x = this.cam.getWidth()/2;
		int y = this.cam.getHeight()/5;
		int width = this.cam.getWidth()/5;
		int height = this.cam.getHeight()/5;
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/victory.png", x, y, width, height, 5);
	}


	@Override
	protected void playerHasLost() {
		removeCurrentHUDTextImage();
		int x = this.cam.getWidth()/2;
		int y = this.cam.getHeight()/5;
		int width = this.cam.getWidth()/5;
		int height = this.cam.getHeight()/5;
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", x, y, width, height, 5);
	}


	@Override
	protected void gameIsDrawn() {
		removeCurrentHUDTextImage();
		int x = this.cam.getWidth()/2;
		int y = this.cam.getHeight()/5;
		int width = this.cam.getWidth()/5;
		int height = this.cam.getHeight()/5;
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", x, y, width, height, 5);
	}


	@Override
	protected IHUD createAndGetHUD() {
		hud = new MoonbaseAssaultHUD(this, this.getCamera());
		return hud;
	}


	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		int x = this.cam.getWidth()/2;
		int y = this.cam.getHeight()/5;
		int width = this.cam.getWidth()/5;
		int height = this.cam.getHeight()/5;
		switch (newStatus) {
		case SimpleGameData.ST_WAITING_FOR_PLAYERS:
			removeCurrentHUDTextImage();
			if (!Globals.HIDE_BELLS_WHISTLES) {
				currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/waitingforplayers.png", x, y, width, height, 3);
			}
			break;
		case SimpleGameData.ST_DEPLOYING:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/getready.png", x, y, width, height, 3);
			break;
		case SimpleGameData.ST_STARTED:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/missionstarted.png", x, y, width, height, 3);
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
			currentHUDTextImage = null;
		}
	}


	@Override
	protected Spatial getPlayersWeaponModel() {
		if (!Globals.HIDE_BELLS_WHISTLES) {
			Spatial model = assetManager.loadModel("Models/pistol/pistol.blend");
			JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/pistol/pistol_tex.png");
			model.scale(0.1f);
			// x moves l-r, z moves further away
			//model.setLocalTranslation(-0.35f, -.3f, .5f);
			model.setLocalTranslation(-0.20f, -.2f, 0.4f);
			return model;
		} else {
			return null;
		}
	}


	/*
	@Override
	protected Spatial getPlayersWeaponModel() {
		//if (!Globals.HIDE_BELLS_WHISTLES) {
			Spatial model = assetManager.loadModel("Models/QuaterniusGuns/Pistol.blend");
			JMEAngleFunctions.rotateToDirection(model, new Vector3f(0, 0, -1));
			//JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/pistol/pistol_tex.png");
			model.scale(0.3f);
			// x moves l-r, z moves further away
			//model.setLocalTranslation(-0.20f, -.2f, 0.4f);
			//model.setLocalTranslation(-0.20f, -.2f, 1.8f);
			model.setLocalTranslation(-0.20f, -.2f, 1.2f);
			return model;
		/*} else {
			return null;
		}*/
	//}
	
	
	@Override
	protected Class[] getListofMessageClasses() {
		return new Class[] {HudDataMessage.class, MoonbaseAssaultGameData.class};
	}


	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (name.equalsIgnoreCase(TEST)) {
			if (value) {
			}
		} else {
			super.onAction(name, value, tpf);
		}
	}


}
