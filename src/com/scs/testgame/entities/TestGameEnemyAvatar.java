package com.scs.testgame.entities;

import com.jme3.scene.Spatial;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoverzombie.models.ZombieModel;

public class TestGameEnemyAvatar extends AbstractEnemyAvatar {

	private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);
	private ZombieModel zm;

	public TestGameEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, pid, eid, x, y, z);
	}

	
	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		if (zm == null) {
			zm = new ZombieModel(game.getAssetManager());
		}
		return zm.getModel();

	}


	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animList;
	}

	
	@Override
	public void setCurrentAnim(String s) {
		this.currentAnim = s;
		this.zm.channel.setAnim(s);
	}

}
