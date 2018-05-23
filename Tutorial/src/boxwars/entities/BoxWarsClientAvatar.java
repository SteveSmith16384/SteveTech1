package boxwars.entities;

import com.jme3.renderer.Camera;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.input.IInputDevice;

import boxwars.BoxWarsServer;
import boxwars.models.BoxAvatarModel;

public class BoxWarsClientAvatar extends AbstractClientAvatar {

	public BoxWarsClientAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, IHUD _hud, int eid, float x, float y, float z, int side, float _moveSpeed, float _jumpSpeed) {
		super(_module, BoxWarsServer.AVATAR, _playerID, _input, _cam, _hud, eid, x, y, z, side, new BoxAvatarModel(_module.getAssetManager()), _moveSpeed, _jumpSpeed);
		
	}

}
