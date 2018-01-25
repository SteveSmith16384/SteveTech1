package com.scs.stevetech1.systems;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IAnimated;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.HistoricalAnimationData;

/**
 * This runs client-side, and checks that an IAnimated entity is running the correct animation.
 *
 */
public class AnimationSystem extends AbstractSystem {

	private AbstractGameClient client;

	public AnimationSystem(AbstractGameClient _client) {
		client = _client;
	}


	public void process(IAnimated anim, float tpf_secs) {
		if (anim.getAnimList() != null) { // Might be unanimated
			HistoricalAnimationData had = anim.getAnimList().get(client.renderTime, true);
			if (had != null) {
				if (!had.animation.equals(anim.getCurrentAnim())) {
					try {
						anim.setCurrentAnim(had.animation);
						//this.zm.channel.setAnim(had.animation);
					} catch (IllegalArgumentException ex) {
						Globals.pe(ex.getMessage());
					}
				}
			}
		}
	}


}
