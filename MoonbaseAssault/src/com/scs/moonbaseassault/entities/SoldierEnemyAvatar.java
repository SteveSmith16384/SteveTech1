package com.scs.moonbaseassault.entities;

import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;

public class SoldierEnemyAvatar extends AbstractEnemyAvatar {
	
	private ChronologicalLookup<HistoricalAnimationData> animData = new ChronologicalLookup<HistoricalAnimationData>(true, 500);
	private SoldierModel snowman;
	
	// Animation
	//public boolean showDied = false;
	
	public SoldierEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, pid, eid, x, y, z, new SoldierModel(game.getAssetManager()));
		
		this.snowman = (SoldierModel)anim;
	}
	

	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animData;
	}
	

	@Override
	public void setCurrentAnimForCode(String s) {
		if (Globals.DEBUG_PLAYER_RESTART) {
			//Globals.p("SnowmanEnemyAvatar: Showing anim " + s);
		}
		this.currentAnimCode = s;
	}

	
	@Override
	public String getCurrentAnimCode() {
		return this.currentAnimCode;
	}


	@Override
	public void processAnimation(float tpf_secs) {
		// todo
	}



}
