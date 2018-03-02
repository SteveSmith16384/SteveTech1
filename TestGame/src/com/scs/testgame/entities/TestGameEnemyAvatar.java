package com.scs.testgame.entities;

import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;
import com.scs.testgame.models.CharacterModel;

public class TestGameEnemyAvatar extends AbstractEnemyAvatar {

	private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);
	private CharacterModel model;

	public TestGameEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z, int side) {
		super(game, TestGameClientEntityCreator.AVATAR, pid, eid, x, y, z, new CharacterModel(game.getAssetManager()), side);
		
		model = (CharacterModel)super.anim;
	}

	/*
	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		return model.getModel(true);

	}
*/

	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animList;
	}

	@Override
	public void setAnimCode(int s) {
		
	}

	
	@Override
	public void processAnimation(float tpf_secs) {
		
	}


}
