package com.scs.testgame;

import java.io.IOException;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.AbstractCollisionValidator;
import com.scs.testgame.entities.Crate;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.House;
import com.scs.testgame.entities.Terrain1;
import com.scs.testgame.entities.TestGameServerAvatar;
import com.scs.testgame.entities.Wall;

public class TestGameServer extends AbstractGameServer {

	public static final String GAME_ID = "Test Game";
	
	private AbstractCollisionValidator collisionValidator = new AbstractCollisionValidator();

	public static void main(String[] args) {
		try {
			AbstractGameServer app = new TestGameServer();
			//app.setPauseOnLostFocus(false);
			//app.start(JmeContext.Type.Headless);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public TestGameServer() throws IOException {
		super(GAME_ID, new GameOptions(10*1000, 5*60*1000, 10*1000, 
				TestGameStaticData.GAME_IP_ADDRESS, TestGameStaticData.GAME_PORT, //TestGameStaticData.LOBBY_IP_ADDRESS, TestGameStaticData.LOBBY_PORT, 
				5, 5),
				25, 40, 200, Integer.MAX_VALUE);//, -5f, 0.99f);
		start(JmeContext.Type.Headless);
	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		avatar.setWorldTranslation(new Vector3f(3f, 26f, 3f + (avatar.playerID*2)));
	}


	@Override
	protected void createGame() {
		super.gameData = new SimpleGameData(nextGameID.getAndAdd(1));
		
		Floor floor = new Floor(this, getNextEntityID(), 0, 0, 0, 30, .5f, 30, "Textures/floor015.png", null);
		this.actuallyAddEntity(floor);
		
		//Terrain1 terrain = new Terrain1(this, getNextEntityID(), 0, 0, 0);
		//this.actuallyAddEntity(terrain);
		/*
		Crate c = new Crate(this, getNextEntityID(), 1, 30, 1, 1, 1, 1f, "Textures/crate.png", 45);
		this.actuallyAddEntity(c);
		c = new Crate(this, getNextEntityID(), 1, 30, 6, 1, 1, 1f, "Textures/crate.png", 65);
		this.actuallyAddEntity(c);
		c = new Crate(this, getNextEntityID(), 6, 30, 1, 1, 1, 1f, "Textures/crate.png", 45);
		this.actuallyAddEntity(c);
		c = new Crate(this, getNextEntityID(), 6, 30, 6, 1, 1, 1f, "Textures/crate.png", 65);
		this.actuallyAddEntity(c);
		
		Wall w1 = new Wall(this, getNextEntityID(), 0, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
		this.actuallyAddEntity(w1);
		Wall w2 = new Wall(this, getNextEntityID(), 10, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
		this.actuallyAddEntity(w2);
		Wall w3 = new Wall(this, getNextEntityID(), 20, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
		this.actuallyAddEntity(w3);
		Wall w4 = new Wall(this, getNextEntityID(), 30, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 270);
		this.actuallyAddEntity(w4);
*/
		//new MovingTarget(this, getNextEntityID(), 2, 2, 10, 1, 1, 1, "Textures/seamless_bricks/bricks2.png", 0);
		//new RoamingZombie(this, getNextEntityID(), 2, 2, 10);
		
		//House house = new House(this, getNextEntityID(), 20, 0, 20, 0);
		//this.actuallyAddEntity(house);
	}


	@Override
	public float getAvatarStartHealth(AbstractAvatar avatar) {
		return 1;
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		return new TestGameServerAvatar(this, client, client.getPlayerID(), client.remoteInput, entityid);
	}


	@Override
	protected int getWinningSide() {
		return 0;
	}


	@Override
	protected Class[] getListofMessageClasses() {
		return null;
	}


	@Override
	public int getSide(ClientData client) {
		return 1;
	}


	@Override
	public boolean doWeHaveSpaces() {
		return true;
	}


	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		return collisionValidator.canCollide(a, b);
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		if (pa.type == TestGameClientEntityCreator.TERRAIN1 || pb.type == TestGameClientEntityCreator.TERRAIN1) {
			//Globals.p("Collision between " + pa + " and " + pb);
		}

		super.collisionOccurred(a, b);

	}


	@Override
	public int getMinPlayersRequiredForGame() {
		return 1;
	}


}
