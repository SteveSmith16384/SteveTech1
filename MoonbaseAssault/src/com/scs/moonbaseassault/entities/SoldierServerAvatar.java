package com.scs.moonbaseassault.entities;

import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IEntityController;

public class SoldierServerAvatar extends AbstractServerAvatar {//implements IUnit {
	
	public SoldierServerAvatar(IEntityController _module, ClientData client, int _playerID, IInputDevice _input, int eid) {
		super(_module, client, _playerID, _input, eid, new SoldierModel(_module.getAssetManager()));
	}
	
}