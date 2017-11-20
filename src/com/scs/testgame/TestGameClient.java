package com.scs.testgame;

import java.util.prefs.BackingStoreException;

import com.jme3.system.AppSettings;
import com.scs.stetech1.client.GenericClient;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.server.Settings;

public class TestGameClient extends GenericClient {

	private TestGameEntityCreator creator;
	
	public static void main(String[] args) {
		try {
			settings = new AppSettings(true);
			try {
				settings.load(Settings.NAME);
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
			settings.setUseJoysticks(true);
			settings.setAudioRenderer(null); // Avoid error with no soundcard
			settings.setTitle(Settings.NAME);// + " (v" + Settings.VERSION + ")");
			if (Settings.SHOW_LOGO) {
				//settings.setSettingsDialogImage("/game_logo.png");
			} else {
				settings.setSettingsDialogImage(null);
			}

			GenericClient app = new TestGameClient();
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
				settings.save(Settings.NAME);
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			Settings.p("Error: " + e);
			e.printStackTrace();
		}

	}


	public TestGameClient() {
		super();
		
		creator = new TestGameEntityCreator(this);
	}


	@Override
	protected IEntity createEntity(NewEntityMessage msg) {
		return creator.createEntity(msg);
	}
	
}
