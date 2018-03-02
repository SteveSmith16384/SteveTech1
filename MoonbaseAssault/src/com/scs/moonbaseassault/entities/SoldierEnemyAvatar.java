package com.scs.moonbaseassault.entities;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;

public class SoldierEnemyAvatar extends AbstractEnemyAvatar implements AnimEventListener {
	
	private ChronologicalLookup<HistoricalAnimationData> animData = new ChronologicalLookup<HistoricalAnimationData>(true, 500);
	private SoldierModel soldier;

	public SoldierEnemyAvatar(IEntityController game, int type, int pid, int eid, float x, float y, float z, int side) {
		super(game, type, pid, eid, x, y, z, new SoldierModel(game.getAssetManager()), side);
		
		this.soldier = (SoldierModel)anim;
	}
	

	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animData;
	}
	

	@Override
	public void setAnimCode(int animCode) {
		//Globals.p("SoldierEnemyAvatar: setCurrentAnimForCode(" + s + ")");
		if (animCode != this.currentAnimCode) {
			soldier.setAnim(animCode);
		}
		this.currentAnimCode = animCode;
	}

	/*
	@Override
	public String getCurrentAnimCode() {
		return this.currentAnimCode;
	}
*/

	@Override
	public void processAnimation(float tpf_secs) {
		// Do nothing, JME handles it
	}


	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		if (animName.equals("Jump")) {
			soldier.isJumping = false;
			this.currentAnimCode = AbstractAvatar.ANIM_IDLE;
		}
	}


	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// Do nothing
	}


}
