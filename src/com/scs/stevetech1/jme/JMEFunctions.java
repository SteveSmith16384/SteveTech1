package com.scs.stevetech1.jme;

import java.io.File;
import java.io.IOException;

import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.TextureKey;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.stevetech1.server.Globals;

public class JMEFunctions {

	private JMEFunctions() {
	}


	public static Spatial LoadModel(AssetManager assetManager, String path) {
		boolean LOAD_JME_VERSION = true;

		Spatial ship = null;
		String j30_path = path.substring(path.lastIndexOf("/")+1) + ".j3o";
		try {
			if (LOAD_JME_VERSION) {
				String filename = "Models/" + j30_path;
				System.out.println("Loading " + filename);
				//}
			}
		} catch (AssetNotFoundException | IllegalArgumentException ex) {
			// Do nothing
		}
		if (ship == null) {
			System.err.println("WARNING!! Loading original model! " + path);
			ship = assetManager.loadModel(path);
			File file = new File("assets/Models/" + j30_path);
			BinaryExporter exporter = BinaryExporter.getInstance();
			try {
				exporter.save(ship, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ship;
	}


	public static Geometry GetGrid(AssetManager assetManager, int size){
		Geometry g = new Geometry("wireframe grid", new Grid(size, size, 1f) );
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", ColorRGBA.White);
		g.setMaterial(mat);
		//g.center().move(pos);
		//rootNode.attachChild(g);
		g.move(-size/2, 0, -size/2);
		return g;
	}


	public static void TurnTowards_Gentle(Spatial spatial, Vector3f target, float pcent) {
		if (pcent <= 0) {
			return; //throw new RuntimeException("Invalid pcent: " + pcent);
		}
		if (pcent > 1) {
			pcent = 1;
		}
		Vector3f dir_to_target = target.subtract(spatial.getWorldTranslation()).normalizeLocal();
		Quaternion target_q = new Quaternion();
		target_q.lookAt(dir_to_target, Vector3f.UNIT_Y);
		Quaternion our_q = spatial.getWorldRotation();
		Quaternion new_q = new Quaternion();
		if (target_q.dot(our_q) > 0.99f) {
			// Just look at it
			new_q = target_q;
		} else {
			new_q.slerp(our_q, target_q, pcent);
		}
		spatial.setLocalRotation(new_q);
	}


	public static void MoveForwards(Spatial spatial, float speed) {
		Vector3f forward = spatial.getLocalRotation().mult(Vector3f.UNIT_Z);
		Vector3f offset = forward.mult(speed);
		spatial.move(offset);
	}


	public static BitmapText CreateBitmapText(BitmapFont guiFont, String text) {
		BitmapText bmp = new BitmapText(guiFont, false);
		bmp.setSize(guiFont.getCharSet().getRenderedSize());
		bmp.setColor(ColorRGBA.White);
		bmp.setText(text);
		return bmp;

	}


	public static float GetAngleBetween(Spatial spatial, Vector3f target) {
		Vector3f dir_to_target = target.subtract(spatial.getWorldTranslation()).normalizeLocal();
		Vector3f forward = spatial.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal();
		float diff = forward.distance(dir_to_target);
		return diff;
	}


	public static void SetTextureOnSpatial(AssetManager assetManager, Spatial spatial, String tex) {
		TextureKey key3 = new TextureKey(tex);
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material material = null;
		if (Globals.LIGHTING) {
			material = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			material.setTexture("DiffuseMap", tex3);
		} else {
			material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			material.setTexture("ColorMap", tex3);
		}
		SetMaterialOnSpatial(spatial, material);
	}


	public static void SetMaterialOnSpatial(Spatial spatial, Material mat) {
		if (spatial instanceof Node) {
			Node node = (Node) spatial;
			for (Spatial s : node.getChildren()) {
				SetMaterialOnSpatial(s, mat);
			}
		} else {
			Geometry g = (Geometry)spatial;
			g.setMaterial(mat);
		}
	}

}
