package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.models.SmallExplosionModel;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.shared.IEntityController;

public class ExplosionEffectEntity extends PhysicalEntity implements IProcessByClient {

	private static final float DURATION = 1;
	
	private SmallExplosionModel expl;
	private float timeLeft = DURATION;
	
	public ExplosionEffectEntity(IEntityController _game, int id, Vector3f pos) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.EXPLOSION_EFFECT, "Explosion", true, false);

		this.setWorldTranslation(pos);
		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		} else {
			expl = new SmallExplosionModel(_game.getAssetManager(), _game.getRenderManager());
			this.mainNode.attachChild(expl);
		}

	}

	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		expl.process();
		timeLeft -= tpf_secs;
		if (timeLeft <= 0) {
			this.remove();
		}
	}

}
