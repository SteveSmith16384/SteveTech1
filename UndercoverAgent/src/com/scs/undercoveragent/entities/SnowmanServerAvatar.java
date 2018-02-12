package com.scs.undercoveragent.entities;

import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.models.SnowmanModel;

public class SnowmanServerAvatar extends AbstractServerAvatar {
	
	public SnowmanServerAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid, int side) {
		super(_module, _playerID, _input, eid, side, new SnowmanModel(_module.getAssetManager()));
	}
	
	
	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		super.processByServer(server, tpf);
		
		if (this.alive) {
			long timeSinceMove = System.currentTimeMillis() - super.lastMoveTime;
			if (timeSinceMove > 5 * 1000) {
				//Globals.p("Forcing client to jump");
				this.jump();  //Send message?  But client might ignore it
			}
		}
	}


}
