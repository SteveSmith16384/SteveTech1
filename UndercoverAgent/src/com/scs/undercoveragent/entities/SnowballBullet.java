package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingSphere;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IClientControlled;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRemoveOnContact;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.EntityLaunchedMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.shared.PositionCalculator;
import com.scs.stevetech1.systems.client.LaunchData;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

public class SnowballBullet extends PhysicalEntity implements IProcessByClient, ILaunchable, IRemoveOnContact, ICausesHarmOnContact, IClientControlled {

	public PositionCalculator clientSidePositionData = new PositionCalculator(true, 500); // So we know where we were in the past to compare against where the server says we should have been
	private boolean launched = false;
	public IEntity shooter; // So we know who not to collide with
	private int side;

	public SnowballBullet(IEntityController _game, int id, IRequiresAmmoCache<SnowballBullet> owner, int _side) {
		super(_game, id, UndercoverAgentClientEntityCreator.SNOWBALL_BULLET, "Snowball", true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
			creationData.put("containerID", owner.getID());
		}

		if (owner != null) { // Only snowball fired by us have an owner
			owner.addToCache(this);
		}

		side = _side;
		
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

		ball_geo.setModelBound(new BoundingSphere());
		this.mainNode.attachChild(ball_geo); //ball_geo.getModelBound();

		this.getMainNode().setUserData(Globals.ENTITY, this);

		//syncPos = new AdjustByFractionOfDistance();

		this.collideable = false;

		//game.addEntity(this);
	}


	public void launch(IEntity _shooter, Vector3f startPos, Vector3f dir) {
		if (launched) { // We might be the client that fired the bullet, we we've already launched
			Globals.p("Snowball already launched.  This may be a good sign.");
			return;
		}
		
		if (_shooter == null) {
			throw new RuntimeException("Null launcher");
		}
		
		
		launched = true;
		shooter = _shooter;
		
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);
		this.simpleRigidBody.setBounciness(0f);
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(10));

		game.getRootNode().attachChild(this.mainNode);
		this.setWorldTranslation(startPos);
		this.mainNode.updateGeometricState();

		this.collideable = true;
		
		// If server, send messages to clients to tell them it has been launched
		if (game.isServer()) {
			AbstractGameServer server = (AbstractGameServer)game;
			LaunchData ld = new LaunchData(startPos, dir, shooter.getID(), System.currentTimeMillis());
			server.networkServer.sendMessageToAll(new EntityLaunchedMessage(this.getID(), ld));
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
			//this.storeAvatarPosition(client.getServerTime());
			simpleRigidBody.process(tpf_secs); //this.mainNode;
		}
	}


	@Override
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse, float tpf_secs) {
		// Do nothing!
	}

	
	@Override
	public boolean sendUpdates() {
		return false; 
	}



	/*
	private void storeAvatarPosition(long serverTime) {
		Vector3f pos = getWorldTranslation();
		//Globals.p("Storing pos " + pos);
		this.clientSidePositionData.addPositionData(pos, null, serverTime);
	}
*/

	@Override
	public IEntity getLauncher() {
		return shooter;
	}


	@Override
	public float getDamageCaused() {
		return 1;
	}


	@Override
	public int getSide() {
		return side;//shooter.getSide();
	}


	@Override
	public boolean isClientControlled() {
		return launched; // All launched snowballs are under client control
	}

}
