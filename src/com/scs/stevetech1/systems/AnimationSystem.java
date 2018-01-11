package com.scs.stevetech1.systems;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IAnimated;
import com.scs.stevetech1.shared.HistoricalAnimationData;

public class AnimationSystem extends AbstractSystem {

	private AbstractGameClient client;
	
	public AnimationSystem(AbstractGameClient _client) {
		client = _client;
	}
	
	
	public void process(IAnimated anim, float tpf_secs) {
		HistoricalAnimationData had = anim.getAnimList().get(client.renderTime, true);
		if (had != null) {
			if (!had.animation.equals(anim.getCurrentAnim())) {
				anim.setCurrentAnim(had.animation);
				//this.zm.channel.setAnim(had.animation);
			}
		}		
	}


}
