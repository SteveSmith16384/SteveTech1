package com.scs.undercoveragent.entities;

import com.jme3.scene.Spatial;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.models.SnowmanModel;

public class SnowmanEnemyAvatar extends AbstractEnemyAvatar {
	
	private Spatial model;
	
	public SnowmanEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, pid, eid, x, y, z);
		
		SnowmanModel tmp = new SnowmanModel(game.getAssetManager());
		model = tmp.getModel(true);
	}
	

	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return null;
	}
	

	@Override
	public void setCurrentAnim(String s) {
		// Do nothing
		
	}

	
	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		return model;
	}

}
