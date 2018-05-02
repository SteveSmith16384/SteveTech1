package twoweeks.entities;

import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;

import twoweeks.abilities.PlayersMachineGun;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.models.SoldierModel;
import twoweeks.server.TwoWeeksServer;

public class MercServerAvatar extends AbstractServerAvatar {
	
	public MercServerAvatar(TwoWeeksServer _module, ClientData client, IInputDevice _input, int eid) {
		super(_module, TwoWeeksClientEntityCreator.SOLDIER_AVATAR, client, _input, eid, new SoldierModel(_module.getAssetManager()), 100f);
		
		IAbility abilityGun = new PlayersMachineGun(_module, _module.getNextEntityID(), playerID, this, eid, 0, client);
		_module.actuallyAddEntity(abilityGun);
		
		//IAbility abilityGrenades = new GrenadeLauncher(_module, _module.getNextEntityID(), this, 1, client);
		//_module.actuallyAddEntity(abilityGrenades);

	}
	
}