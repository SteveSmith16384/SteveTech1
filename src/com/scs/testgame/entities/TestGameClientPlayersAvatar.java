package com.scs.testgame.entities;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.client.AbstractGameClient;
import com.scs.stetech1.entities.ClientPlayersAvatar;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.IEntityController;

public class TestGameClientPlayersAvatar extends ClientPlayersAvatar {

	/*private static final float PLAYER_HEIGHT = 0.7f;
	private static final float PLAYER_RAD = 0.2f;
	private static final float WEIGHT = 3f;
*/
	public TestGameClientPlayersAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, float x, float y, float z, byte side) {
		super(_module, _playerID, _input, _cam, _hud, eid, x, y, z, side);
	}


	public static Spatial getPlayersModel_Static(IEntityController game, int pid) {
		Box box1 = new Box(PLAYER_RAD, PLAYER_HEIGHT/2, PLAYER_RAD);
		Geometry playerGeometry = new Geometry("Player", box1);
		TextureKey key3 = new TextureKey("Textures/neon1.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		Material floor_mat = null;
		if (Settings.LIGHTING) {
			floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floor_mat.setTexture("DiffuseMap", tex3);
		} else {
			floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			floor_mat.setTexture("ColorMap", tex3);
		}
		playerGeometry.setMaterial(floor_mat);
		return playerGeometry;
		
	}


	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		return getPlayersModel_Static(game, pid);
	}


}
