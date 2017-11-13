package com.scs.stetech1.client.entities;

import java.util.HashMap;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.stetech1.components.IAffectedByPhysics;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.entities.PhysicalEntity;

public class EnemyPlayersAvatar extends PhysicalEntity implements IAffectedByPhysics, ICollideable {// Need ICollideable so lasers don't bounce off it

	//private HashMap<String, Object> creationData;// = new HashMap<String, Object>();

	public EnemyPlayersAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, eid, EntityTypes.AVATAR, "EnemyAvatar");

		/*if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("id", eid);
			creationData.put("playerID", eid);
		}*/

		Spatial geometry = AbstractPlayersAvatar.getPlayersModel(game, pid);
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
		rigidBodyControl.setKinematic(true);
		main_node.addControl(rigidBodyControl);

		game.getBulletAppState().getPhysicsSpace().add(rigidBodyControl);
		game.getRootNode().attachChild(this.main_node);

		geometry.setUserData(Settings.ENTITY, this);
		main_node.setUserData(Settings.ENTITY, this);
		rigidBodyControl.setUserObject(this);

		game.addEntity(this);

		this.setWorldTranslation(new Vector3f(x, y, z));

	}


	/*@Override
	public void setWorldTranslation(Vector3f pos) {
		this.rigidBodyControl.setPhysicsLocation(pos.clone());
		this.getMainNode().setLocalTranslation(pos.x, pos.y, pos.z);
	}
*/

	@Override
	public void process(float tpf) {
		//Settings.p("Pos: " + this.getLocation());
	}


	@Override
	public void collidedWith(ICollideable other) {
		// Do nothing
	}


}
