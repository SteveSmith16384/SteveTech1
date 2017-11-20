package com.scs.testgame;

import java.io.IOException;

import com.jme3.system.JmeContext;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.server.Settings;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.Wall;

public class TestGameServer extends ServerMain {
	
	public static void main(String[] args) {
		try {
			ServerMain app = new TestGameServer();
			app.setPauseOnLostFocus(false);
			if (Settings.HEADLESS_SERVER) {
				app.start(JmeContext.Type.Headless);
			} else {
				app.start();				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public TestGameServer() throws IOException {
		super();
	}


	protected void createGame() {
		new Floor(this, getNextEntityID(), 0, 0, 0, 30, .5f, 30, "Textures/floor015.png", null);
		//new DebuggingSphere(this, getNextEntityID(), 0, 0, 0);
		//new Crate(this, getNextEntityID(), 8, 2, 8, 1, 1, 1f, "Textures/crate.png", 45);
		//new Crate(this, getNextEntityID(), 8, 5, 8, 1, 1, 1f, "Textures/crate.png", 65);
		new Wall(this, getNextEntityID(), 0, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
	}


}
