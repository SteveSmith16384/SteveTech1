package myjme3test.helloworld;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import com.scs.stevetech1.jme.JMEFunctions;

public class CustomMeshViewer extends SimpleApplication {

	public static void main(String[] args){
		CustomMeshViewer app = new CustomMeshViewer();
		app.showSettings = false;

		app.start();
	}


	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default
		//assetManager.registerLocator("assets/Textures/", FileLocator.class);
		cam.setFrustumPerspective(60, settings.getWidth() / settings.getHeight(), .1f, 100);
		this.flyCam.setMoveSpeed(12f);
		
		//setupLight();

		/*
2			3
0,3,0--3,3,0
| \        |
|   \      |
|     \    |
|       \  |
|         \|
0,0,0--3,0,0
0			1

		 */
		Vector3f [] vertices = new Vector3f[4];
		vertices[0] = new Vector3f(0,0,0);
		vertices[1] = new Vector3f(3,0,0);
		vertices[2] = new Vector3f(0,3,0);
		vertices[3] = new Vector3f(3,3,0);
		
		Vector2f[] texCoord = new Vector2f[4];
		texCoord[0] = new Vector2f(0,0);
		texCoord[1] = new Vector2f(1,0);
		texCoord[2] = new Vector2f(0,1);
		texCoord[3] = new Vector2f(1,1);
		
		int [] indexes = { 2,0,1, 1,3,2 };
		
		Mesh mesh = new Mesh();

		mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
		mesh.setBuffer(Type.Index,    3, BufferUtils.createIntBuffer(indexes));

		float[] normals = new float[12];
		normals = new float[]{0,0,1, 0,0,1, 0,0,1, 0,0,1};
		mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));

		mesh.updateBound();
		
		
		Geometry geo = new Geometry("OurMesh", mesh); // using our custom mesh object
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		geo.setMaterial(mat);
		rootNode.attachChild(geo);
		
		
		this.rootNode.attachChild(JMEFunctions.GetGrid(assetManager, 10));
		rootNode.updateGeometricState();

	}


	private void setupLight() {
		// Remove existing lights
		this.rootNode.getWorldLightList().clear();
		LightList list = this.rootNode.getWorldLightList();
		for (Light it : list) {
			this.rootNode.removeLight(it);
		}

		// We add light so we see the scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(4f));
		rootNode.addLight(al);

		DirectionalLight dirlight = new DirectionalLight(); // FSR need this for textures to show
		rootNode.addLight(dirlight);

	}


	@Override
	public void simpleUpdate(float tpf) {
		//System.out.println("Pos: " + this.cam.getLocation());
		//this.rootNode.rotate(0,  tpf,  tpf);
	}


}