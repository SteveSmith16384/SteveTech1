package boxwars.entities;

import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;

import boxwars.BoxWarsServer;
import boxwars.models.BoxAvatarModel;

public class BoxWarsServerAvatar extends AbstractServerAvatar {
	
	public BoxWarsServerAvatar(BoxWarsServer _module, ClientData _client, IInputDevice _input, int eid) {
		super(_module, BoxWarsServer.AVATAR, _client, _input, eid, new BoxAvatarModel(_module.getAssetManager()), 1f, 3f, 2f);

		//IAbility abilityGun = new LaserRifle(_module, _module.getNextEntityID(), client.getPlayerID(), this, eid, 0, client);
		//todo _module.actuallyAddEntity(abilityGun);
		
	}
	

}
