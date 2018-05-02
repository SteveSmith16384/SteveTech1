package com.scs.moonbaseassault.entities;

import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.moonbaseassault.weapons.GrenadeLauncher;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.weapons.HitscanRifle;

public class SoldierServerAvatar extends AbstractServerAvatar { //implements IUnit {
	
	public SoldierServerAvatar(MoonbaseAssaultServer _module, ClientData client, IInputDevice _input, int eid) {
		super(_module, MoonbaseAssaultClientEntityCreator.SOLDIER_AVATAR, client, _input, eid, new SoldierModel(_module.getAssetManager()), 100f);
		
		//IAbility abilityGun = new LaserRifle(_module, _module.getNextEntityID(), client.getPlayerID(), this, eid, 0, client);
		IAbility abilityGun = new HitscanRifle(_module, _module.getNextEntityID(), MoonbaseAssaultClientEntityCreator.HITSCAN_RIFLE, client.getPlayerID(), this, eid, 0, client, MoonbaseAssaultClientEntityCreator.BULLET_TRAIL, MoonbaseAssaultClientEntityCreator.DEBUGGING_SPHERE);
		_module.actuallyAddEntity(abilityGun);
		
		IAbility abilityGrenades = new GrenadeLauncher(_module, _module.getNextEntityID(), client.getPlayerID(), this, eid, 1, client);
		_module.actuallyAddEntity(abilityGrenades);

	}
	
}