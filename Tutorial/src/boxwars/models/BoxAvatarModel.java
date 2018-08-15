package boxwars.models;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.stevetech1.components.IAvatarModel;

public class BoxAvatarModel implements IAvatarModel {

	private static final float w = 0.3f;
	private static final float d = 0.3f;
	private static final float h = 0.7f;
	
	private Geometry geometry;
	private Vector3f size;
	
	public BoxAvatarModel(AssetManager assetManager) {
		super();
		
		size = new Vector3f(w, h, d);

		Box box1 = new Box(w/2, h/2, d/2);
		//box1.scaleTextureCoordinates(new Vector2f(WIDTH, HEIGHT));
		geometry = new Geometry("MovingTarget", box1);

		TextureKey key3 = new TextureKey("Textures/football.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material floor_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		floor_mat.setTexture("DiffuseMap", tex3);
		geometry.setMaterial(floor_mat);

		geometry.setLocalTranslation(0, h, 0); // Origin is at the bottom

	}

	@Override
	public Spatial createAndGetModel() {
		return geometry;
	}

	@Override
	public Spatial getModel() {
		return geometry;
	}

	@Override
	public Vector3f getSize() {
		return size;
	}

	@Override
	public float getCameraHeight() {
		return h - 0.2f;
	}

	@Override
	public float getBulletStartHeight() {
		return h - 0.3f;
	}

	@Override
	public void setAnim(int anim) {
		// Not animated
	}
}
