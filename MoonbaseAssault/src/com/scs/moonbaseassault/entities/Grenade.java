package com.scs.moonbaseassault.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingSphere;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class Grenade extends AbstractBullet {
	
	private static final float DURATION = 3f;
	
	private float timeLeft = DURATION;

	public Grenade(IEntityController _game, int id, IEntityContainer<Grenade> owner, int _side, ClientData _client) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.GRENADE, "Grenade", owner, _side, _client);

		Sphere sphere = new Sphere(8, 8, 0.07f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("grenade", sphere);

		if (!_game.isServer()) { // Not running in server
			ball_geo.setShadowMode(ShadowMode.CastAndReceive);
			TextureKey key3 = new TextureKey( "Textures/grenade.png");
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
		this.mainNode.attachChild(ball_geo);
		this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		if (launched) {
			super.processByServer(server, tpf_secs);
			Globals.p("Grenade Y:" + this.getWorldTranslation().y);
			this.timeLeft -= tpf_secs;
			if (this.timeLeft <= 0) {
				// todo - explode
				// this.remove();
			}
		}
	}


	@Override
	public float getDamageCaused() {
		return 0;
	}


	@Override
	protected void createSimpleRigidBody(Vector3f dir) {
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);
		this.simpleRigidBody.setAerodynamicness(0.98f); // Don't roll forever
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(10));

	}


}
