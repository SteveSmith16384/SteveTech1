package com.scs.testgame.entities;

import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;
import com.scs.testgame.models.CharacterModel;

public class TestGameEnemyAvatar extends AbstractOtherPlayersAvatar {

	//private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);
	private CharacterModel model;

	public TestGameEnemyAvatar(IEntityController game, int eid, float x, float y, float z, byte side) {
		super(game, TestGameClientEntityCreator.AVATAR, eid, x, y, z, new CharacterModel(game.getAssetManager()), side, "TestGame");
		
		model = (CharacterModel)super.anim;
	}


}
