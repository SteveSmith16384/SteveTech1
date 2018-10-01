package com.scs.testgame;

import com.jme3.util.SkyFactory;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.AbstractSimpleGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;

public class TestGameClient extends AbstractSimpleGameClient {

	private TestGameClientEntityCreator creator;

	public static void main(String[] args) {
		try {
			new TestGameClient();
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}

	}


	private TestGameClient() {
		super("Test Game", "localhost", TestGameServer.GAME_PORT, "My Name");
		start();
	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		creator = new TestGameClientEntityCreator();

		getGameNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
		
	}
	

	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg) {
		return creator.createEntity(client, msg);
	}

}
