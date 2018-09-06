package boxwars.entities;

import com.scs.stevetech1.avatartypes.PersonAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;

import boxwars.BoxWarsServer;
import boxwars.models.BoxAvatarModel;
import boxwars.weapons.PlayersGun;

public class BoxWarsServerAvatar extends AbstractServerAvatar {
	
	public BoxWarsServerAvatar(BoxWarsServer _module, ClientData _client, IInputDevice _input, int eid) {
		super(_module, BoxWarsServer.AVATAR, _client, _input, eid, new BoxAvatarModel(_module.getAssetManager()), 1f, 1, new PersonAvatar(_module, _input, 3f, 2f));

		IAbility abilityGun = new PlayersGun(_module, _module.getNextEntityID(), client.getPlayerID(), this, eid, (byte)0, client);
		_module.actuallyAddEntity(abilityGun);
		
	}


	@Override
	public void updateClientSideHealth(int amt) {
		// Do nothing
		
	}
	

}
