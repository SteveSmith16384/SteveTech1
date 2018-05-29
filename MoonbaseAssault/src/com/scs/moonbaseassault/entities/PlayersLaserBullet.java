package com.scs.moonbaseassault.entities;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class PlayersLaserBullet extends AbstractPlayersBullet implements INotifiedOfCollision {

	private static final float LENGTH = .7f;
	private static final float RANGE = 30f;

	public PlayersLaserBullet(IEntityController _game, int id, int playerOwnerId, IEntityContainer<AbstractPlayersBullet> owner, int _side, ClientData _client, Vector3f dir) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.PLAYER_LASER_BULLET, "LaserBullet", playerOwnerId, owner, _side, _client, dir, true, AILaserBullet.SPEED, RANGE);

		this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	protected void createSimpleRigidBody(Vector3f dir) {
		Spatial laserNode = null;
		Vector3f origin = Vector3f.ZERO;
		laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(LENGTH)), ColorRGBA.Pink, !game.isServer(), "Textures/greensun.jpg", MoonbaseAssaultServer.LASER_DIAM, Globals.BULLETS_CONES);

		//laserNode.setShadowMode(ShadowMode.Cast);
		this.mainNode.attachChild(laserNode);

	}

	/*
	@Override
	public float getDamageCaused() {
		//return ((RANGE-this.getDistanceTravelled()) / this.getDistanceTravelled()) * 10;
		float dam = (((RANGE-this.getDistanceTravelled()) / this.getDistanceTravelled()) * 5)+5; 
		Globals.p(this + " damage: " + dam);
		return dam;
	}
	 */

	@Override
	public void collided(PhysicalEntity pe) {
		if (game.isServer()) {
			Globals.p("PlayerLaserBullet collided");
			AbstractGameServer server = (AbstractGameServer)game;
			String tex = "Textures/sun.jpg";
			if (pe instanceof IDebrisTexture) {
				IDebrisTexture dt = (IDebrisTexture)pe;
				tex = dt.getDebrisTexture();
			}
			server.sendExplosion(this.getWorldTranslation(), 4, .8f, 1.2f, .005f, .02f, tex);
		}
		this.remove();
	}

}
