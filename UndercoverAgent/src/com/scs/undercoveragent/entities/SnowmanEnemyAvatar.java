package com.scs.undercoveragent.entities;

import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.models.SnowmanModel;

public class SnowmanEnemyAvatar extends AbstractEnemyAvatar {
	
	private ChronologicalLookup<HistoricalAnimationData> animData = new ChronologicalLookup<HistoricalAnimationData>(true, 500);
	private SnowmanModel snowman;
	
	// Animation
	public boolean showDied = false;
	
	public SnowmanEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, pid, eid, x, y, z, new SnowmanModel(game.getAssetManager()));
		
		this.snowman = (SnowmanModel)anim;
	}
	

	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animData;
	}
	

	@Override
	public void animCodeChanged(String s) {
		if (s.equals(AbstractAvatar.ANIM_DIED)) {
			this.showDied = true;
		} else {
			this.showDied = false;
		}
		this.currentAnimCode = s;
	}

	
	@Override
	public String getCurrentAnimCode() {
		return this.currentAnimCode;
	}


	@Override
	public void processAnimation(float tpf_secs) {
		if (this.showDied) {
			this.snowman.showDied(tpf_secs);
		} else {
			this.snowman.showAlive(tpf_secs);
		}
		
	}



}
