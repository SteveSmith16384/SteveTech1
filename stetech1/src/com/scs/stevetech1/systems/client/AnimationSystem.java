package com.scs.stevetech1.systems.client;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IClientSideAnimated;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.systems.AbstractSystem;

/**
 * This runs client-side, and checks that an IAnimated entity is running the correct animation.
 *
 */
public class AnimationSystem extends AbstractSystem {

	private AbstractGameClient client;

	public AnimationSystem(AbstractGameClient _client) {
		client = _client;
	}


	public void process(IClientSideAnimated anim, float tpf_secs) {
		HistoricalAnimationData had = anim.getAnimList().get(client.renderTime, true);
		if (had != null) {
			if (!had.animationCode.equals(anim.getCurrentAnimCode())) { // Has the animation changed?
				try {
					anim.animCodeChanged(had.animationCode);
				} catch (IllegalArgumentException ex) {
					Globals.pe(ex.getMessage());
				}
			}
		}
		anim.processAnimation(tpf_secs);
	}


}
