package boxwars.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import boxwars.BoxWarsServer;

public class Floor extends PhysicalEntity implements IProcessByClient {

	private Box box1;
	//private float w, h, d;

	public Floor(IEntityController _game, int id, float x, float yTop, float z, float w, float h, float d, String tex, Vector3f _texScroll) {
		super(_game, id, BoxWarsServer.FLOOR, "Floor", false, true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("pos", new Vector3f(x, yTop, z));
			creationData.put("size", new Vector3f(w, h, d));
			creationData.put("tex", tex);
		}

		/*this.w = w;
		this.h = h;
		this.d = d;
*/
		box1 = new Box(w/2, h/2, d/2);

		box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
				0, h, w, h, w, 0, 0, 0, // back
				0, h, d, h, d, 0, 0, 0, // right
				0, h, w, h, w, 0, 0, 0, // front
				0, h, d, h, d, 0, 0, 0, // left
				w, 0, w, d, 0, d, 0, 0, // top
				w, 0, w, d, 0, d, 0, 0  // bottom
		}));

		Geometry geometry = new Geometry("FloorGeom", box1);
		if (!_game.isServer()) { // Not running in server
			TextureKey key3 = new TextureKey(tex);
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floor_mat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(floor_mat);
		}
		this.mainNode.attachChild(geometry);
		geometry.setLocalTranslation((w/2), -(h/2), (d/2)); // Move it into position
		mainNode.setLocalTranslation(x, yTop, z); // Move it into position

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		//this.simpleRigidBody.setMovable(false);
		simpleRigidBody.setNeverMoves(true);

		geometry.setUserData(Globals.ENTITY, this);
	}


	@Override
	public void processByClient(IClientApp client, float tpf) {
		// Do nothing
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		// Do nothing

	}


}
