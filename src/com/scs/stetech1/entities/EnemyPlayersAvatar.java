package com.scs.stetech1.entities;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.stetech1.client.GenericClient;
import com.scs.stetech1.components.IAffectedByPhysics;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public abstract class EnemyPlayersAvatar extends PhysicalEntity implements IAffectedByPhysics, ICollideable, IProcessByClient {// Need ICollideable so lasers don't bounce off it

	//private HashMap<String, Object> creationData;// = new HashMap<String, Object>();

	public EnemyPlayersAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, eid, EntityTypes.AVATAR, "EnemyAvatar");

		/*if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("id", eid);
			creationData.put("playerID", eid);
		}*/

		Spatial geometry = getPlayersModel(game, pid);
		/*Box box1 = new Box(2, 2, 2);
		//box1.scaleTextureCoordinates(new Vector2f(WIDTH, HEIGHT));
		Geometry geometry = new Geometry("Crate", box1);

			TextureKey key3 = new TextureKey("Textures/crate.png");//Settings.getCrateTex());//"Textures/boxes and crates/" + i + ".png");
			key3.setGenerateMips(true);
			Texture tex3 = module.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = null;
			if (Settings.LIGHTING) {
				floor_mat = new Material(module.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(module.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}

			geometry.setMaterial(floor_mat);
			floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			geometry.setQueueBucket(Bucket.Transparent);

*/
		/*if (!game.isServer()) { // Not running in server
			TextureKey key3 = new TextureKey(tex);//Settings.getCrateTex());//"Textures/boxes and crates/" + i + ".png");
			key3.setGenerateMips(true);
			Texture tex3 = module.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = null;
			if (Settings.LIGHTING) {
				floor_mat = new Material(module.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(module.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}

			geometry.setMaterial(floor_mat);
			floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			geometry.setQueueBucket(Bucket.Transparent);
		}*/

		this.main_node.attachChild(geometry);
		//float rads = (float)Math.toRadians(rotDegrees);
		//main_node.rotate(0, rads, 0);

		rigidBodyControl = new RigidBodyControl(0f); // Only the server can move them!
		main_node.addControl(rigidBodyControl);
		rigidBodyControl.setKinematic(true);

		game.getBulletAppState().getPhysicsSpace().add(rigidBodyControl);
		game.getRootNode().attachChild(this.main_node);

		geometry.setUserData(Settings.ENTITY, this);
		main_node.setUserData(Settings.ENTITY, this);
		rigidBodyControl.setUserObject(this);

		game.addEntity(this);

		this.setWorldTranslation(new Vector3f(x, y, z));

	}


	protected abstract Spatial getPlayersModel(IEntityController game, int pid);


	@Override
	public void process(ServerMain sevrer, float tpf) {
		//Settings.p("Pos: " + this.getLocation());
	}


	@Override
	public void collidedWith(ICollideable other) {
		// Do nothing
	}


	@Override
	public boolean canMove() {
		return true; // Always calc for avatars
	}


	@Override
	public void process(GenericClient client, float tpf_secs) {
		// Do nothing?
	}


}
