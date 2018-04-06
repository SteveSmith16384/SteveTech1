package com.scs.undercoveragent.entities;

import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.models.SnowmanModel;

public class SnowmanEnemyAvatar extends AbstractEnemyAvatar {
	
	//private ChronologicalLookup<HistoricalAnimationData> animData = new ChronologicalLookup<HistoricalAnimationData>(true, 500);
	private SnowmanModel snowman;
	private int currentAnimCode = -1;

	// Animation
	public boolean showDied = false;
	
	public SnowmanEnemyAvatar(IEntityController game, int type, int pid, int eid, float x, float y, float z, int side) {
		super(game, type, pid, eid, x, y, z, new SnowmanModel(game.getAssetManager()), side);
		
		this.snowman = (SnowmanModel)anim;
	}
	
/*
	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animData;
	}
	*/

	@Override
	public void setAnimCode(int s) {
		if (s == AbstractAvatar.ANIM_DIED) {
			this.showDied = true;
		} else {
			this.showDied = false;
		}
		this.currentAnimCode = s;
	}


	@Override
	public void processManualAnimation(float tpf_secs) {
		if (this.showDied) {
			this.snowman.showDied(tpf_secs);
		} else {
			this.snowman.showAlive(tpf_secs);
		}
		
	}

/*
	@Override
	public int getCurrentAnimCode() {
		return currentAnimCode;
	}
*/

}
