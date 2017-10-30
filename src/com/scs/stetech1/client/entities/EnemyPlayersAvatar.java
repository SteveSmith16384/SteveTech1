package com.scs.stetech1.client.entities;

import java.util.HashMap;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Spatial;
import com.scs.stetech1.components.IAffectedByPhysics;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class EnemyPlayersAvatar extends PhysicalEntity implements IAffectedByPhysics, ICollideable {// Need ICollideable so lasers don't bounce off it

	private HashMap<String, Object> creationData = new HashMap<String, Object>();

	public EnemyPlayersAvatar(IEntityController game, int pid, int eid) {
		super(game, eid, EntityTypes.AVATAR, "EnemyAvatar");

		creationData.put("id", eid);
		creationData.put("playerID", eid);

		Spatial geometry = AbstractPlayersAvatar.getPlayersModel(game, pid);
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

		rigidBodyControl = new RigidBodyControl(1f);
		main_node.addControl(rigidBodyControl);
		
		module.getBulletAppState().getPhysicsSpace().add(rigidBodyControl);
		module.getRootNode().attachChild(this.main_node);

		geometry.setUserData(Settings.ENTITY, this);
		main_node.setUserData(Settings.ENTITY, this);
		rigidBodyControl.setUserObject(this);

		module.addEntity(this);

	}


	@Override
	public void process(float tpf) {
		//Settings.p("Pos: " + this.getLocation());
	}


	@Override
	public void collidedWith(ICollideable other) {
		// Do nothing
	}


	@Override
	public HashMap<String, Object> getCreationData() {
		return creationData;
	}


}
