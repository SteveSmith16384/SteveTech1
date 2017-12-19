package com.scs.testgame.entities;


import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.client.AbstractGameClient;
import com.scs.stetech1.client.HistoricalPositionCalculator;
import com.scs.stetech1.client.syncposition.ICorrectClientEntityPosition;
import com.scs.stetech1.client.syncposition.InstantPositionAdjustment;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.components.IRequiresAmmoCache;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.PositionCalculator;
import com.scs.testgame.TestGameEntityCreator;

public class Grenade extends PhysicalEntity implements IProcessByClient {

	private float timeLeft = 4f;

	private ICorrectClientEntityPosition syncPos;
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500); // So we know where we were in the past to compare against where the server says we should have been

	public Grenade(IEntityController _game, int id, IRequiresAmmoCache<Grenade> owner) {
		super(_game, id, TestGameEntityCreator.GRENADE, "Grenade");

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("side", side);
			creationData.put("containerID", owner.getID());
		}
		
		Sphere sphere = new Sphere(8, 8, 0.1f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("grenade", sphere);

		if (game.getJmeContext() != JmeContext.Type.Headless) { // Not running in server
			TextureKey key3 = new TextureKey( "Textures/grenade.png");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material floor_mat = null;
			if (Settings.LIGHTING) {
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}
			ball_geo.setMaterial(floor_mat);
		}

		this.mainNode.attachChild(ball_geo);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);
		this.simpleRigidBody.setBounciness(.6f);

		this.getMainNode().setUserData(Settings.ENTITY, this);
		game.addEntity(this);

		syncPos = new InstantPositionAdjustment();
	}

	
	public void launch(ICanShoot shooter) {
		game.getRootNode().attachChild(this.mainNode);
		this.setWorldTranslation(shooter.getBulletStartPos());
		this.simpleRigidBody.setLinearVelocity(shooter.getShootDir().normalize().mult(5));
	}

	
	@Override
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse) {
		//SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody; 
		//simplePlayerControl.getAdditionalForce().set(0, 0, 0);
		if (Settings.SYNC_GRENADE_POS) {
			Vector3f offset = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, serverTimeToUse, mainApp.pingRTT/2);
			if (offset != null) {
				this.syncPos.adjustPosition(this, offset);
			}
		}
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		this.timeLeft -= tpf_secs;
		if (this.timeLeft < 0) {
			// todo - damage surrounding entities
			this.remove();
		}
		super.processByServer(server, tpf_secs);
	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		simpleRigidBody.process(tpf_secs);

		this.timeLeft -= tpf_secs;
		if (this.timeLeft < 0) {
			//todo game.doExplosion(this.getWorldTranslation(), this);//, 3, 10);
			this.remove();
		}
		//super.processByServer(null, tpf_secs);

	}

}
