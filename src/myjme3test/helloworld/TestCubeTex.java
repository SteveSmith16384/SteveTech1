package myjme3test.helloworld;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.material.Material;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.TextureCubeMap;
import com.jme3.util.BufferUtils;

public class TestCubeTex extends SimpleApplication {

	private Box box1;
	private float offx = 0;
	float w = 5f;
	float h = 5f;
	float d = 1f;


	public static void main(String[] args) {
		TestCubeTex app = new TestCubeTex();
		//app.settings = new AppSettings(true);
		app.showSettings = false;
		app.start();
	}

	
	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default

		flyCam.setMoveSpeed(40);

		/** just a blue box floating in space */
		box1 = new Box(w/2, h/2, d/2);
		
		//box1.sett
		//box1.scaleTextureCoordinates(new Vector2f(10, 10));
		Geometry geometry = new Geometry("Crate", box1);
		TextureKey key3 = new TextureKey("Textures/tron1.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = getAssetManager().loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material floor_mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		floor_mat.setTexture("ColorMap", tex3);
		
		//floor_mat.getAdditionalRenderState().setWireframe(true);
		//box1.setLineWidth(5);
		//floor_mat.getAdditionalRenderState().setDepthTest(false);
		//floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Additive);
		//floor_mat.getAdditionalRenderState().setPointSprite(true);
		//floor_mat.getAdditionalRenderState().setColorWrite(colorWrite)
		
		geometry.setMaterial(floor_mat);
		//floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		//geometry.setQueueBucket(Bucket.Transparent);

		this.rootNode.attachChild(geometry);
		
		box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
				0, h, w, h, w, 0, 0, 0, // back
				0, h, d, h, d, 0, 0, 0, // right
		        .5f, h+.5f, w, h+.5f, w, .5f, .5f, .5f, // front
		        0, h, d, h, d, 0, 0, 0, // left
		        w, 0, w, d, 0, d, 0, 0, // top
		        w, 0, w, d, 0, d, 0, 0  // bottom
				}));

/*top works!		box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
		        1, 0, 0, 0, 0, 1, 1, 1, // back
		        1, 0, 0, 0, 0, 1, 1, 1, // right
		        1, 0, 0, 0, 0, 1, 1, 1, // front
		        1, 0, 0, 0, 0, 1, 1, 1, // left
		        w, 0, w, d, 0, d, 0, 0, // top
		        1, 0, 0, 0, 0, w, h, w  // bottom
				}));

	/*	box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
				1, 0, 0, 0, 0, 0.25f, 1, 0.25f, // back
				1, 0, 0, 0, 0, 0.25f, 1, 0.25f, // right
				1, 0, 0, 0, 0, 0.25f, 1, 0.25f, // front
				1, 0, 0, 0, 0, 0.25f, 1, 0.25f, // left
				1, 0, 0, 0, 0, 1, 1, 1, // top
				1, 0, 0, 0, 0, 1, 1, 1 // bottom
				}));
*/


		/*if (Settings.RECORD_VID) {
			Settings.p("Recording video");
			VideoRecorderAppState video_recorder = new VideoRecorderAppState();
			stateManager.attach(video_recorder);
		}*/
		
		TextureKey cmkey = new TextureKey("Textures/map.png");//testcubemap.jpg");
		Texture cmtex = getAssetManager().loadTexture(cmkey);
        Image img = cmtex.getImage();
        TextureCubeMap cubemap = new TextureCubeMap();
        cubemap.setImage(img);
	
		//getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/map.png", false));
		
		
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		BloomFilter laser_bloom = new BloomFilter(BloomFilter.GlowMode.Scene);
		//bloom2.setDownSamplingFactor(2f);
		laser_bloom.setBlurScale(5f);
		laser_bloom.setBloomIntensity(50);
		fpp.addFilter(laser_bloom);
		this.viewPort.addProcessor(fpp);
	
	}


	/** Move the listener with the a camera - for 3D audio. */
	@Override
	public void simpleUpdate(float tpf) {
		offx += 0.1f * tpf;
		
		box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
				0, h, w, h, w, 0, 0, 0, // back
				0, h, d, h, d, 0, 0, 0, // right
				offx, h, w+offx, h, w+offx, 0, offx, 0, // front
		        0, h, d, h, d, 0, 0, 0, // left
		        w, 0, w, d, 0, d, 0, 0, // top
		        w, 0, w, d, 0, d, 0, 0  // bottom
				}));

	}

}