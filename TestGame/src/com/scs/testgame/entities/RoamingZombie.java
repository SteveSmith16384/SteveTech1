package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;
import com.scs.testgame.models.ZombieModel;

public class RoamingZombie extends PhysicalEntity implements IAffectedByPhysics, IDamagable, INotifiedOfCollision, IRewindable {
	
	private static final float w = .5f;
	private static final float d = .7f;
	private static final float h = .5f;

	//private static final float DURATION = 3;
	private static final float SPEED = .22f; // 3f

	private ZombieModel zm;
	
	private Vector3f currDir = new Vector3f(1f, 0, 0);
	//private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);

	public RoamingZombie(IEntityController _game, int id, float x, float y, float z) {
		super(_game, id, TestGameClientEntityCreator.ZOMBIE, "Zombie", true, false, true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		Spatial spatial = null;
		if (!_game.isServer()) { // Not running in server
			//spatial = game.getAssetManager().loadModel("Models/zombie/Zombie.blend");
			//JMEFunctions.SetTextureOnSpatial(game.getAssetManager(), spatial, "Models/zombie/ZombieTexture.png");
			zm = new ZombieModel(game.getAssetManager());
			spatial = zm.createAndGetModel();
		} else {
			// Server
			Box box1 = new Box(w/2, h/2, d/2);
			spatial = new Geometry("RoamingZombie", box1);
			spatial.setLocalTranslation(0, h/2, 0); // Box origin is the centre*/
			//zm = new ZombieModel(game.getAssetManager());
			//spatial = zm.getModel(false);

		}
		this.mainNode.attachChild(spatial);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);

		spatial.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		//game.getRootNode().attachChild(this.mainNode);
		//game.addEntity(this);

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		this.getMainNode().lookAt(this.getWorldTranslation().add(currDir), Vector3f.UNIT_Y); // Point us in the right direction
		this.simpleRigidBody.setAdditionalForce(this.currDir.mult(SPEED));
		//this.currentAnim = "ZombieWalk";

		super.processByServer(server, tpf_secs);
	}


	@Override
	public void fallenOffEdge() {
		this.respawn();
	}


	private void respawn() {
		this.setWorldTranslation(new Vector3f(10, 10, 10));

		EntityUpdateMessage eum = new EntityUpdateMessage();
		eum.addEntityData(this, true, this.createEntityUpdateDataRecord());
		AbstractGameServer server = (AbstractGameServer)this.game;
		server.sendMessageToInGameClients(eum);

	}
	
	
	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		//this.currentAnim = "ZombieBite";
	}


	@Override
	public byte getSide() {
		return 0;
	}


	@Override
	public void notifiedOfCollision(PhysicalEntity pe) {
		/*if (pe instanceof Floor == false) {
			Globals.p("Zombie has hit " + pe);
		}*/
		// turn around?		
	}

/*
	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animList;
	}

/*
	@Override
	public void setCurrentAnim(Object s) {
		
	}

/*
	@Override
	public void setCurrentAnim(String s) {
		this.currentAnim = s;
		this.zm.channel.setAnim(s);
	}
*/
/*
	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		HistoricalAnimationData had = this.animList.get(client.renderTime, true);
		if (had != null) {
			if (!had.animation.equals(this.currentAnim)) {
				this.currentAnim = had.animation;
				this.zm.channel.setAnim(had.animation);
			}
		}		
	}
*/
/*
	@Override
	public void addAnim(HistoricalAnimationData had) {
		this.animList.addData(had);
		
	}
*/
	
	@Override
	public float getHealth() {
		return 0;
	}


	@Override
	public void updateClientSideHealth(int amt) {
		
	}
	

}
