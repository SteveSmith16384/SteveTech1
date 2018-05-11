package com.scs.undercoveragent.entities;

import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.models.SnowmanModel;

public class SnowmanEnemyAvatar extends AbstractEnemyAvatar {
	
	private SnowmanModel snowman;

	// Animation
	public boolean showDied = false;
	
	public SnowmanEnemyAvatar(IEntityController game, int eid, float x, float y, float z, int side, String playerName) {
		super(game, UndercoverAgentClientEntityCreator.AVATAR, eid, x, y, z, new SnowmanModel(game.getAssetManager()), side, playerName);
		
		this.snowman = (SnowmanModel)anim;
	}
	

	@Override
	public void setAnimCode(int s) {
		if (s == AbstractAvatar.ANIM_DIED) {
			this.showDied = true;
		} else {
			this.showDied = false;
		}
		//this.currentAnimCode = s;
	}


	@Override
	public void processManualAnimation(float tpf_secs) {
		if (this.showDied) {
			this.snowman.showDied(tpf_secs);
		} else {
			this.snowman.showAlive(tpf_secs);
		}
		
	}


}
