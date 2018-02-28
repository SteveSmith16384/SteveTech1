package com.scs.moonbaseassault.entities;

import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;

public class SoldierEnemyAvatar extends AbstractEnemyAvatar {
	
	private ChronologicalLookup<HistoricalAnimationData> animData = new ChronologicalLookup<HistoricalAnimationData>(true, 500);
	private SoldierModel soldier;
	
	public SoldierEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, pid, eid, x, y, z, new SoldierModel(game.getAssetManager()));
		
		this.soldier = (SoldierModel)anim;
	}
	

	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animData;
	}
	

	@Override
	public void animCodeChanged(String animCode) {
		//Globals.p("SoldierEnemyAvatar: setCurrentAnimForCode(" + s + ")");
		if (!animCode.equalsIgnoreCase(this.currentAnimCode)) { // todo - needed?
			soldier.setAnim(animCode);
		}
		this.currentAnimCode = animCode;
	}

	
	@Override
	public String getCurrentAnimCode() {
		return this.currentAnimCode;
	}


	@Override
	public void processAnimation(float tpf_secs) {
		// Do nothing
	}



}
