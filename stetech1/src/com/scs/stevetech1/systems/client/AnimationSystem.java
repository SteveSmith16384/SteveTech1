package com.scs.stevetech1.systems.client;

import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.EntityUpdateData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.systems.AbstractSystem;

/**
 * This runs client-side, and checks that an IAnimated entity is running the correct animation.
 *
 */
public class AnimationSystem extends AbstractSystem {

	private IClientApp client;

	public AnimationSystem(IClientApp _client) {
		client = _client;
	}


	public void process(IAnimatedClientSide anim, float tpf_secs) {
		//HistoricalAnimationData had = anim.getAnimList().get(client.renderTime, true);
		PhysicalEntity pe = (PhysicalEntity)anim;
		EntityUpdateData had = pe.chronoUpdateData.get(client.getRenderTime(), true);
		if (had != null) {
			//if (had.animationCode != .equals(anim.getCurrentAnimCode())) { // Has the animation changed?
				try {
					anim.setAnimCode(had.animationCode);
				} catch (IllegalArgumentException ex) {
					Globals.pe(ex.getMessage());
				}
			//}
		} else {
			Globals.p("No anim data for " + pe);
		}
		anim.processManualAnimation(tpf_secs);
	}


}
