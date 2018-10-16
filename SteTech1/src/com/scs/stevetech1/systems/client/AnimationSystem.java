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


	public void process(IAnimatedClientSide anim, float tpfSecs) {
		PhysicalEntity pe = (PhysicalEntity)anim;
		EntityUpdateData had = pe.chronoUpdateData.get(client.getRenderTime(), true);
		if (had != null) {
			try {
				anim.setAnimCode_ClientSide(had.animationCode);
			} catch (IllegalArgumentException ex) {
				Globals.pe(ex.getMessage());
			}
		}
		anim.processManualAnimation_ClientSide(tpfSecs);
	}


}
