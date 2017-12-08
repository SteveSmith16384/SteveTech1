package com.scs.testgame.entities;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.components.IBullet;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.models.BeamLaserModel;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class LaserBullet extends PhysicalEntity implements IBullet {

	public ICanShoot shooter;
	private float timeLeft = 3;

	public LaserBullet(IEntityController _game, int id, ICanShoot _shooter, float x, float y, float z) {
		super(_game, id, EntityTypes.LASER_BULLET, "LaserBullet");

		this.shooter = _shooter;

		Vector3f origin = shooter.getBulletStartPos().clone();//getWorldTranslation().clone();
		//origin.addLocal(shooter.getBulletStartOffset());

		Node laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(shooter.getShootDir().multLocal(1)), ColorRGBA.Pink, game.getJmeContext() != JmeContext.Type.Headless);

		this.mainNode.attachChild(laserNode);
		game.getRootNode().attachChild(this.mainNode);
		//laserNode.setLocalTranslation(shooter.getWorldTranslation().add(shooter.getShootDir().multLocal(AbstractPlayersAvatar.PLAYER_RAD*3)));
		//laserNode.getLocalTranslation().y -= 0.1f; // Drop bullets slightly
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);
		if (_game.isServer()) {
			// Accelerate the physical ball to shoot it.
			simpleRigidBody.setLinearVelocity(shooter.getShootDir().mult(30));
			simpleRigidBody.setAerodynamicness(1);
			simpleRigidBody.setGravity(0);
		}
		
		this.getMainNode().setUserData(Settings.ENTITY, this);
		laserNode.setUserData(Settings.ENTITY, this);
		game.addEntity(this);

	}


	@Override
	public void process(AbstractGameServer server, float tpf) {
		if (game.isServer()) {
			this.timeLeft -= tpf;
			if (this.timeLeft < 0) {
				this.remove();
			}
		}
	}


	@Override
	public ICanShoot getShooter() {
		return shooter;
	}


	@Override
	public boolean collidedWith(ICollideable other) {
		if (other != this.shooter) {
			//Settings.p("Laser collided with " + other);

			/*if (Settings.SHOW_FLASH_EXPLOSIONS) {
				Explosion expl = new Explosion(module, game.getRootNode(), game.getAssetManager(), game.getRenderManager(), .05f);
				expl.setLocalTranslation(this.getLocation());
				module.addEntity(expl);
			}

			CubeExplosionShard.Factory(game, module, this.getLocation(), 3);
			 */
			this.remove(); // Don't bounce
		}
		return false;
	}


	@Override
	public float getDamageCaused() {
		return 10;
	}


}
