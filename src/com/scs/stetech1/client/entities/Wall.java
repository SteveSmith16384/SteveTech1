package com.scs.stetech1.client.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.stetech1.components.IAffectedByPhysics;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class Wall extends PhysicalEntity implements IAffectedByPhysics, ICollideable { // Need ICollideable so lasers don't bounce off it

	public Wall(IEntityController _game, float x, float yBottom, float z, String tex, float rotDegrees) {
		super(_game, EntityTypes.WALL, "Wall");

		float w = 3f;
		float h = 1f;
		float d = 0.1f;

		Box box1 = new Box(w/2, h/2, d/2);
		//box1.scaleTextureCoordinates(new Vector2f(WIDTH, HEIGHT));
		Geometry geometry = new Geometry("Wall", box1);
		//int i = NumberFunctions.rnd(1, 10);
		TextureKey key3 = new TextureKey(tex);// Settings.getCrateTex());//"Textures/boxes and crates/" + i + ".png");
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

		this.main_node.attachChild(geometry);
		if (rotDegrees != 0) {
			float rads = (float)Math.toRadians(rotDegrees);
			main_node.rotate(0, rads, 0);
		}
		main_node.setLocalTranslation(x+(w/2), yBottom+(h/2), z+(d/2));

		floor_phy = new RigidBodyControl(0f);
		main_node.addControl(floor_phy);
		module.getBulletAppState().getPhysicsSpace().add(floor_phy);

		geometry.setUserData(Settings.ENTITY, this);
		main_node.setUserData(Settings.ENTITY, this);
		floor_phy.setUserObject(this);

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



}
