package com.scs.testgame.entities;


import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.components.IBullet;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class Grenade extends PhysicalEntity { //implements IBullet {

	public ICanShoot shooter;
	private float timeLeft = 4f;

	/*
	 * Constructor for server
	 */
	public Grenade(IEntityController _game, int id, ICanShoot _shooter) {
		this(_game, id, new Vector3f(_shooter.getBulletStartPos()));//getWorldTranslation().add(_shooter.getBulletStartOffset())));

		this.shooter = _shooter;

		// Accelerate the physical ball to shoot it.
		//if (_game.isServer()) {
			this.simpleRigidBody.setLinearVelocity(shooter.getShootDir().normalize().mult(5));
			//this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(5));
			this.simpleRigidBody.setBounciness(.6f);
		//}

	}


	/*
	 * Constructor for client
	 */
	public Grenade(IEntityController _game, int id, Vector3f origin) {
		super(_game, id, EntityTypes.GRENADE, "Grenade");

		Sphere sphere = new Sphere(8, 8, 0.1f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("cannon ball", sphere);

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
		game.getRootNode().attachChild(this.mainNode);
		mainNode.setLocalTranslation(origin);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);

		this.getMainNode().setUserData(Settings.ENTITY, this);
		game.addEntity(this);

	}


	@Override
	public void process(AbstractGameServer server, float tpf) {
		if (game.isServer()) {
			this.timeLeft -= tpf;
			if (this.timeLeft < 0) {
				//todo game.doExplosion(this.getWorldTranslation(), this);//, 3, 10);
				this.remove();
			}
		}
	}

/*
	@Override
	public ICanShoot getShooter() {
		return shooter;
	}

	@Override
	public float getDamageCaused() {
		return 0;
	}
*/

}
