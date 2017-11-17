package com.scs.stetech1.shared.entities;


import com.jme3.asset.TextureKey;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import com.scs.stetech1.components.IBullet;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class Grenade extends PhysicalEntity implements IBullet {

	public ICanShoot shooter;
	private float timeLeft = 2f;

	/*
	 * Constructor for server
	 */
	public Grenade(IEntityController _game, int id, ICanShoot _shooter) {
		this(_game, id, _shooter, new Vector3f(_shooter.getWorldTranslation().add(_shooter.getBulletStartOffset())));
	}
	
	
	/*
	 * Constructor for client
	 */
	public Grenade(IEntityController _game, int id, ICanShoot _shooter, Vector3f origin) {
		super(_game, id, EntityTypes.GRENADE, "Grenade");

		this.shooter = _shooter;

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

		this.main_node.attachChild(ball_geo);
		game.getRootNode().attachChild(this.main_node);
		//ball_geo.setLocalTranslation(shooter.getWorldTranslation().add(shooter.getShootDir().multLocal(AbstractPlayersAvatar.PLAYER_RAD*2)));
		ball_geo.setLocalTranslation(origin);
		rigidBodyControl = new RigidBodyControl(.2f);
		if (_game.isServer() || Settings.CLIENT_SIDE_PHYSICS) {
		} else {
			rigidBodyControl.setKinematic(true);
		}
		ball_geo.addControl(rigidBodyControl);
		game.getBulletAppState().getPhysicsSpace().add(rigidBodyControl);
		/** Accelerate the physical ball to shoot it. */
		rigidBodyControl.setLinearVelocity(shooter.getShootDir().mult(15));

		this.getMainNode().setUserData(Settings.ENTITY, this);
		rigidBodyControl.setUserObject(this);
		game.addEntity(this);

	}


	@Override
	public void process(ServerMain server, float tpf) {
		this.timeLeft -= tpf;
		if (this.timeLeft < 0) {
			//todo game.doExplosion(this.getWorldTranslation(), this);//, 3, 10);
			this.remove();
		}

	}


	@Override
	public ICanShoot getShooter() {
		return shooter;
	}


	@Override
	public void collidedWith(ICollideable other) {

	}


	@Override
	public float getDamageCaused() {
		return 0;
	}


}
