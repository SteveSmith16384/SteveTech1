package com.scs.testgame;

import com.jme3.util.SkyFactory;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.ValidClientSettings;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

public class TestGameClient extends AbstractGameClient {

	private TestGameClientEntityCreator creator;
	private AbstractCollisionValidator collisionValidator;

	public static void main(String[] args) {
		try {
			AbstractGameClient app = new TestGameClient();
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}

	}


	private TestGameClient() {
		super(new ValidClientSettings(TestGameServer.GAME_ID, "Key", 1), "test Game", null, //TestGameStaticData.GAME_IP_ADDRESS, TestGameStaticData.GAME_PORT, //null, -1, 
				25, 200, Integer.MAX_VALUE, 1f);
		start();
	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		creator = new TestGameClientEntityCreator();
		collisionValidator = new AbstractCollisionValidator();

		getGameNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
		
		this.connect(TestGameStaticData.GAME_IP_ADDRESS, TestGameStaticData.GAME_PORT, false);

	}
	

	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg) {
		return creator.createEntity(client, msg);
	}

/*
	@Override
	protected Spatial getPlayersWeaponModel() {
		return null;
	}
*/

	@Override
	protected Class[] getListofMessageClasses() {
		return null;
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return collisionValidator.canCollide(a, b);
	}


}
