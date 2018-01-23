package com.scs.undercoveragent;

import java.util.prefs.BackingStoreException;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.server.Globals;

public class UndercoverAgentClient extends AbstractGameClient {

	//private TestGameClientEntityCreator creator;

	public static void main(String[] args) {
		try {
			settings = new AppSettings(true);
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

			AbstractGameClient app = new UndercoverAgentClient();
			//instance = app;
			app.setSettings(settings);
			app.setPauseOnLostFocus(false); // Needs to always be in sync with server!

			/*File video, audio;
			if (Settings.RECORD_VID) {
				//app.setTimer(new IsoTimer(60));
				video = File.createTempFile("JME-water-video", ".avi");
				audio = File.createTempFile("JME-water-audio", ".wav");
				Capture.captureVideo(app, video);
				Capture.captureAudio(app, audio);
			}*/

			app.start();

			/*if (Settings.RECORD_VID) {
				System.out.println("Video saved at " + video.getCanonicalPath());
				System.out.println("Audio saved at " + audio.getCanonicalPath());
			}*/

			try {
				settings.save(Globals.NAME);
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}

	}


	public UndercoverAgentClient() {
		super(UndercoverAgentStaticData.GAME_IP_ADDRESS, UndercoverAgentStaticData.GAME_PORT, new UndercoverAgentClientEntityCreator());
	}


	@Override
	protected void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(.3f));
		getRootNode().addLight(al);

		DirectionalLight sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(.5f, -1f, .5f).normalizeLocal());
		rootNode.addLight(sun);
	}



}
