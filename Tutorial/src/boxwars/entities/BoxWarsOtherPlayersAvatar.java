package boxwars.entities;

import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.shared.IEntityController;

import boxwars.BoxWarsServer;
import boxwars.models.BoxAvatarModel;

public class BoxWarsOtherPlayersAvatar extends AbstractOtherPlayersAvatar {

	public BoxWarsOtherPlayersAvatar(IEntityController game, int eid, float x, float y, float z, byte side) {
		super(game, BoxWarsServer.AVATAR, eid, x, y, z, new BoxAvatarModel(game.getAssetManager()), side, "TestGame");
		
	}



}
