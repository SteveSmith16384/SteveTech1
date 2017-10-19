package com.scs.stetech1.server.entities;

import com.jme3.math.Vector3f;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.IEntityController;

public class ServerPlayersAvatar extends AbstractPlayersAvatar {

	public ServerPlayersAvatar(IEntityController _module, int _playerID, IInputDevice _input) {
		super(_module, _playerID, _input);
	}


	@Override
	public void process(float tpf) {
		if (this.restarting) {
			restartTime -= tpf;
			if (this.restartTime <= 0) {
				this.moveToStartPostion(true);
				restarting = false;
				return;
			}
		}

		super.process(tpf);
	}
	
	
	public void moveToStartPostion(boolean invuln) {
		//Point p = module.mapData.getPlayerStartPos(id);
		Vector3f warpPos = new Vector3f(3f, 3f, 3f);
		Settings.p("Scheduling player to start position: " + warpPos);
		this.playerControl.warp(warpPos);
		if (invuln) {
			//todo invulnerableTime = Sorcerers.properties.GetInvulnerableTimeSecs();
		}
	}



	@Override
	public Vector3f getShootDir() {
		return input.getDirection();
	}


}
