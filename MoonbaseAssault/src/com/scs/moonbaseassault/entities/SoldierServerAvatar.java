package com.scs.moonbaseassault.entities;

import com.scs.moonbaseassault.abilities.LaserRifle;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.moonbaseassault.weapons.GrenadeLauncher;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;

public class SoldierServerAvatar extends AbstractServerAvatar { //implements IUnit {
	
	public SoldierServerAvatar(MoonbaseAssaultServer _module, ClientData client, int _playerID, IInputDevice _input, int eid) {
		super(_module, client, _playerID, _input, eid, new SoldierModel(_module.getAssetManager()));
		
		IAbility abilityGun = new LaserRifle(_module, _module.getNextEntityID(), this, 0, client);
		_module.actuallyAddEntity(abilityGun);

		
		IAbility abilityGrenades = new GrenadeLauncher(_module, _module.getNextEntityID(), this, 1, client);
		_module.actuallyAddEntity(abilityGrenades);

	}
	
}