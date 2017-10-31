package com.scs.stetech1.shared.entities;

import java.util.HashMap;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.stetech1.components.IBullet;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.models.BeamLaserModel;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class LaserBullet extends PhysicalEntity implements IBullet {

	public ICanShoot shooter;
	private float timeLeft = 3;

	public LaserBullet(IEntityController _game, int id, ICanShoot _shooter) {
		super(_game, id, EntityTypes.UNFIRED_BULLET, "LaserBullet");

		this.shooter = _shooter;

		Vector3f origin = shooter.getWorldTranslation().clone();

		Node ball_geo = BeamLaserModel.Factory(module.getAssetManager(), origin, origin.add(shooter.getShootDir().multLocal(1)), ColorRGBA.Pink);

		this.main_node.attachChild(ball_geo);
		module.getRootNode().attachChild(this.main_node);
		/** Position the cannon ball  */
		ball_geo.setLocalTranslation(shooter.getWorldTranslation().add(shooter.getShootDir().multLocal(AbstractPlayersAvatar.PLAYER_RAD*3)));
		ball_geo.getLocalTranslation().y -= 0.1f; // Drop bullets slightly
		/** Make the ball physical with a mass > 0.0f */
		rigidBodyControl = new RigidBodyControl(.1f);
		/** Add physical ball to physics space. */
		ball_geo.addControl(rigidBodyControl);
		module.getBulletAppState().getPhysicsSpace().add(rigidBodyControl);
		/** Accelerate the physical ball to shoot it. */
		rigidBodyControl.setLinearVelocity(shooter.getShootDir().mult(40));
		rigidBodyControl.setGravity(Vector3f.ZERO);

		this.getMainNode().setUserData(Settings.ENTITY, this);
		ball_geo.setUserData(Settings.ENTITY, this);
		rigidBodyControl.setUserObject(this);
		module.addEntity(this);

	}


	@Override
	public void process(float tpf) {
		this.timeLeft -= tpf;
		if (this.timeLeft < 0) {
			this.remove();
		}
	}


	@Override
	public ICanShoot getShooter() {
		return shooter;
	}


	@Override
	public void collidedWith(ICollideable other) {
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
	}


	@Override
	public float getDamageCaused() {
		return 10;
	}


	@Override
	public HashMap<String, Object> getCreationData() {
		return null;
	}

}
