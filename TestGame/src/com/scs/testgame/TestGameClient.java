package com.scs.testgame;

import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

public class TestGameClient extends AbstractGameClient {

	private TestGameClientEntityCreator creator;
	private AbstractCollisionValidator collisionValidator;

	public static void main(String[] args) {
		try {
			/*settings = new AppSettings(true);
			try {
				settings.load(Globals.NAME);
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
			settings.setUseJoysticks(true);
			settings.setAudioRenderer(null); // Avoid error with no soundcard
			settings.setTitle(Globals.NAME);// + " (v" + Settings.VERSION + ")");
			if (Globals.SHOW_LOGO) {
				//settings.setSettingsDialogImage("/game_logo.png");
			} else {
				settings.setSettingsDialogImage(null);
			}
*/
			AbstractGameClient app = new TestGameClient();
			//app.setSettings(settings);
			//app.setPauseOnLostFocus(false); // Needs to always be in sync with server!

			/*File video, audio;
			if (Settings.RECORD_VID) {
				//app.setTimer(new IsoTimer(60));
				video = File.createTempFile("JME-water-video", ".avi");
				audio = File.createTempFile("JME-water-audio", ".wav");
				Capture.captureVideo(app, video);
				Capture.captureAudio(app, audio);
			}*/

			//app.start();

			/*if (Settings.RECORD_VID) {
				System.out.println("Video saved at " + video.getCanonicalPath());
				System.out.println("Audio saved at " + audio.getCanonicalPath());
			}*/
/*
			try {
				settings.save(Globals.NAME);
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
*/
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}

	}


	public TestGameClient() {
		super(TestGameServer.GAME_ID, "test Game", null, TestGameStaticData.GAME_IP_ADDRESS, TestGameStaticData.GAME_PORT, //null, -1, 
				25, 200, Integer.MAX_VALUE, 1f);
		
	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		creator = new TestGameClientEntityCreator();
		collisionValidator = new AbstractCollisionValidator();

		getGameNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));

	}
	
	
	@Override
	protected IHUD getHUD() {
		return null;
	}


	@Override
	protected void playerHasWon() {
		// Do nothing
	}


	@Override
	protected void playerHasLost() {
		// Do nothing
	}


	@Override
	protected void gameIsDrawn() {
		// Do nothing
	}


	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityMessage msg) {
		return creator.createEntity(client, msg);
	}


	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		// Do nothing
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


	@Override
	public void sendMessage(MyAbstractMessage msg) {
		this.networkClient.sendMessageToServer(msg);
		
	}

}
