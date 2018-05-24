package boxwars.entities;

import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.IEntityController;

import boxwars.BoxWarsServer;
import boxwars.models.BoxAvatarModel;

public class BoxWarsEnemyAvatar extends AbstractEnemyAvatar {

	public BoxWarsEnemyAvatar(IEntityController game, int eid, float x, float y, float z, int side) {
		super(game, BoxWarsServer.AVATAR, eid, x, y, z, new BoxAvatarModel(game.getAssetManager()), side, "TestGame");
		
	}

	@Override
	public void setAnimCode(int s) {
		// Not animated
	}

	
	@Override
	public void processManualAnimation(float tpf_secs) {
		// Not animated
	}


}
