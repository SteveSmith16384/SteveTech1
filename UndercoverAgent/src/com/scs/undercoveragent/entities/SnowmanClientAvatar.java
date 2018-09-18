package com.scs.undercoveragent.entities;

import com.jme3.renderer.Camera;
import com.scs.stevetech1.avatartypes.PersonAvatar;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.Globals;
import com.scs.undercoveragent.UASounds;
import com.scs.undercoveragent.UAStaticData;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.models.SnowmanModel;

import ssmith.util.RealtimeInterval;

public class SnowmanClientAvatar extends AbstractClientAvatar {
	
	private RealtimeInterval walkSfxInterval = new RealtimeInterval(2000);
	
	public SnowmanClientAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, int eid, float x, float y, float z, byte side) {
		super(_module, UndercoverAgentClientEntityCreator.AVATAR, _playerID, _input, _cam, eid, x, y, z, side, new SnowmanModel(_module.getAssetManager()), new PersonAvatar(_module, _input, UAStaticData.MOVE_SPEED, UAStaticData.JUMP_FORCE));
	}

	
	@Override
	public void processByClient(IClientApp client, float tpfSecs) {
		super.processByClient(client, tpfSecs);
		
		// Play footstep sfx?
		PersonAvatar person = (PersonAvatar)super.avatarControl;
		if (person.playerWalked && walkSfxInterval.hitInterval()) {
			game.playSound(UASounds.FOOTSTEPS, -1, null, Globals.DEFAULT_VOLUME, false);
		}

		// Play jump?
		if (person.playerJumped) {
			game.playSound(UASounds.JUMP, -1, null, Globals.DEFAULT_VOLUME, false);
		}
}
	
}
