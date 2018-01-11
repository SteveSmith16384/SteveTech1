package com.scs.testgame.entities;

import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.hud.HUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoverzombie.entities.RoamingZombie;
import com.scs.undercoverzombie.models.ZombieModel;

public class TestGameClientAvatar extends AbstractClientAvatar {

	/*private static final float PLAYER_HEIGHT = 0.7f;
	private static final float PLAYER_RAD = 0.2f;
	private static final float WEIGHT = 3f;
	 */

	private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);
	private ZombieModel zm = new ZombieModel(game.getAssetManager());

	public TestGameClientAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, float x, float y, float z, int side) {
		super(_module, _playerID, _input, _cam, _hud, eid, x, y, z, side);
	}

/*
	public static Spatial getPlayersModel_Static(IEntityController game, int pid) {
		Box box1 = new Box(PLAYER_RAD, PLAYER_HEIGHT/2, PLAYER_RAD);
		Geometry playerGeometry = new Geometry("Player", box1);
		if (!game.isServer()) {
			TextureKey key3 = new TextureKey("Textures/neon1.jpg");
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material floor_mat = null;
			if (Globals.LIGHTING) {
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}
			playerGeometry.setMaterial(floor_mat);
		}
		playerGeometry.setLocalTranslation(0, PLAYER_HEIGHT/2, 0); // make our origin the bottom
		return playerGeometry;		
	}

*/
	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		return zm.getModel();
	}

	
	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animList;
	}

	
	@Override
	public void setCurrentAnim(String s) {
		this.currentAnim = s;
		this.zm.channel.setAnim(s);
	}

/*
	@Override
	public void addAnim(HistoricalAnimationData had) {
		this.animList.addData(had);
		
	}
*/
}
