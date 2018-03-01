package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class LaserBullet extends AbstractBullet {// implements IProcessByClient, ICausesHarmOnContact, ILaunchable, IRemoveOnContact, IClientControlled {

	private float timeLeft = 3f;

	public LaserBullet(IEntityController _game, int id, IEntityContainer<LaserBullet> owner, int _side, ClientData _client) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.LASER_BULLET, "LaserBullet", owner, _side, _client);

		this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	public float getDamageCaused() {
		return 10;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (launched) {
			super.processByServer(server, tpf_secs);
			this.timeLeft -= tpf_secs;
			if (this.timeLeft < 0) {
				this.remove();
			}
		}
	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		if (launched) {
			simpleRigidBody.process(tpf_secs);

			this.timeLeft -= tpf_secs;
			if (this.timeLeft < 0) {
				this.remove();
			}
		}
	}


	@Override
	protected void createSimpleRigidBody(Vector3f dir) {
		Vector3f origin = Vector3f.ZERO;
		Node laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(1)), ColorRGBA.Pink, !game.isServer());
		this.mainNode.attachChild(laserNode);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);
		simpleRigidBody.setAerodynamicness(1);
		simpleRigidBody.setGravity(0);
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(20));

	}


}
