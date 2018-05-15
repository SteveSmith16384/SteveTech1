package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.models.BulletExplosionModel;
import com.scs.moonbaseassault.models.SmallExplosionModel;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.shared.IEntityController;

public class BulletExplosionEntity extends PhysicalEntity implements IProcessByClient { // todo - delete

	private static final float DURATION = 5;
	
	private BulletExplosionModel expl;
	private float timeLeft = DURATION;
	
	public BulletExplosionEntity(int delme, IEntityController _game, int id, Vector3f pos) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.BULLET_EXPLOSION_EFFECT, "BulletExplosionEntity", true, false);

		this.setWorldTranslation(pos);
		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		} else {
			expl = new BulletExplosionModel(_game.getAssetManager(), _game.getRenderManager());
			this.mainNode.attachChild(expl);
		}

	}

	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		expl.process(tpf_secs);
		timeLeft -= tpf_secs;
		if (timeLeft <= 0) {
			expl.stop();
			this.remove();
		}
	}

}
