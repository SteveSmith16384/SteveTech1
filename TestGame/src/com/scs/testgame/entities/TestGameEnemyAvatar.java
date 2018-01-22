package com.scs.testgame.entities;

import com.jme3.scene.Spatial;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.models.CharacterModel;

public class TestGameEnemyAvatar extends AbstractEnemyAvatar {

	private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);
	private CharacterModel model;

	public TestGameEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, pid, eid, x, y, z);
		
		model = new CharacterModel(game.getAssetManager());
	}

	
	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		/*if (model == null) {
			model = new CharacterModel(game.getAssetManager());
		}*/
		return model.getModel(true);

	}


	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animList;
	}

	
	@Override
	public void setCurrentAnim(String s) {
		this.currentAnim = s;
		this.model.channel.setAnim(s);
	}

}
