package com.scs.testgame.entities;

import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;
import com.scs.testgame.models.CharacterModel;

public class TestGameEnemyAvatar extends AbstractEnemyAvatar {

	//private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);
	private CharacterModel model;

	public TestGameEnemyAvatar(IEntityController game, int eid, float x, float y, float z, int side) {
		super(game, TestGameClientEntityCreator.AVATAR, eid, x, y, z, new CharacterModel(game.getAssetManager()), side, "TestGame");
		
		model = (CharacterModel)super.anim;
	}

/*
	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animList;
	}
*/
/*
	@Override
	public int getCurrentAnimCode() {
		return -1;
	}
*/

	@Override
	public void setAnimCode(int s) {
		
	}

	
	@Override
	public void processManualAnimation(float tpf_secs) {
		
	}


}
