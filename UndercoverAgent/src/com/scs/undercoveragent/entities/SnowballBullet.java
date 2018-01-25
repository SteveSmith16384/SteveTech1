package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.HistoricalPositionCalculator;
import com.scs.stevetech1.client.syncposition.ICorrectClientEntityPosition;
import com.scs.stevetech1.client.syncposition.InstantPositionAdjustment;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRemoveOnContact;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.shared.PositionCalculator;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

public class SnowballBullet extends PhysicalEntity implements IProcessByClient, ILaunchable, IRemoveOnContact, ICausesHarmOnContact {

	private ICorrectClientEntityPosition syncPos;
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500); // So we know where we were in the past to compare against where the server says we should have been
	private boolean launched = false;
	public ICanShoot shooter; // So we know who not to collide with

	public SnowballBullet(IEntityController _game, int id, IRequiresAmmoCache<SnowballBullet> owner) {
		super(_game, id, UndercoverAgentClientEntityCreator.SNOWBALL_BULLET, "Snowball", true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("side", side);
			creationData.put("containerID", owner.getID());
		}

		if (owner != null) { // Once launched, they have nothing to do with an owner
			owner.addToCache(this);
		}

		Sphere sphere = new Sphere(8, 8, 0.1f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("grenade", sphere);

		if (!_game.isServer()) { // Not running in server
			TextureKey key3 = new TextureKey( "Textures/snow.jpg");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material floor_mat = null;
			if (Globals.LIGHTING) {
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}
			ball_geo.setMaterial(floor_mat);
		}

		this.mainNode.attachChild(ball_geo);

		this.getMainNode().setUserData(Globals.ENTITY, this);
		game.addEntity(this);

		syncPos = new InstantPositionAdjustment();

		this.collideable = false;
	}


	public void launch(ICanShoot _shooter) {
		launched = true;

		shooter = _shooter;
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);
		this.simpleRigidBody.setBounciness(0f);

		game.getRootNode().attachChild(this.mainNode);
		this.setWorldTranslation(_shooter.getBulletStartPos());
		this.simpleRigidBody.setLinearVelocity(_shooter.getShootDir().normalize().mult(10));
		this.collideable = true;

	}


	@Override
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse) {
		if (launched) {
			if (Globals.SYNC_GRENADE_POS) {
				Vector3f offset = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, serverTimeToUse, mainApp.pingRTT/2);
				if (offset != null) {
					this.syncPos.adjustPosition(this, offset);
				}
			}
		}
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (launched) {
			super.processByServer(server, tpf_secs);
		}
	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		if (launched) {
			simpleRigidBody.process(tpf_secs);
		}
	}


	@Override
	public ICanShoot getLauncher() {
		return shooter;
	}


	@Override
	public float getDamageCaused() {
		return 1;
	}


	@Override
	public int getSide() {
		return shooter.getSide();
	}


}
