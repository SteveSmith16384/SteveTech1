package com.scs.undercoveragent.entities;

import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.models.SnowmanModel;

public class SnowmanEnemyAvatar extends AbstractOtherPlayersAvatar {
	
	private SnowmanModel snowman;

	// Manual animation
	public boolean showDied = false;
	
	public SnowmanEnemyAvatar(IEntityController game, int eid, float x, float y, float z, byte side, String playerName) {
		super(game, UndercoverAgentClientEntityCreator.AVATAR, eid, x, y, z, new SnowmanModel(game.getAssetManager()), side, playerName);
		
		this.snowman = (SnowmanModel)model;
		
	}
	
	
	@Override
	public void drawOnHud(Node hud, Camera cam) {
		// Don't show anything
	}
	

	@Override
	public void setAnimCode_ClientSide(int s) {
		// The model has no animations, so we do our own.
		if (s == AbstractAvatar.ANIM_DIED) {
			this.showDied = true;
		} else {
			this.showDied = false;
		}
	}


	@Override
	public void processManualAnimation_ClientSide(float tpf_secs) {
		// The model has no animations, so we do our own.
		if (this.showDied) {
			this.snowman.showDied(tpf_secs);
		} else {
			this.snowman.showAlive(tpf_secs);
		}
		
	}


}
