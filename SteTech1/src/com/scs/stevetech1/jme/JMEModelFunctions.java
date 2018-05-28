package com.scs.stevetech1.jme;

import java.io.File;
import java.io.IOException;

import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
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

public class JMEModelFunctions {

	private JMEModelFunctions() {
	}


	public static Spatial loadModel(AssetManager assetManager, String path) {
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


	public static Geometry getGrid(AssetManager assetManager, int size){
		Geometry g = new Geometry("wireframe grid", new Grid(size, size, 1f) );
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", ColorRGBA.White);
		g.setMaterial(mat);

		// Move to middle
		//g.move(-size/2, 0, -size/2);
		return g;
	}


	public static BitmapText createBitmapText(BitmapFont guiFont, String text) {
		BitmapText bmp = new BitmapText(guiFont, false);
		bmp.setSize(guiFont.getCharSet().getRenderedSize());
		bmp.setColor(ColorRGBA.White);
		bmp.setText(text);
		return bmp;

	}


	public static void setTextureOnSpatial(AssetManager assetManager, Spatial spatial, String tex) {
		TextureKey key3 = new TextureKey(tex);
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material material = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		material.setTexture("DiffuseMap", tex3);
		setMaterialOnSpatial(spatial, material);
	}


	public static void setTextureOnSpatial(AssetManager assetManager, Spatial spatial, Texture tex3) {
		tex3.setWrap(WrapMode.Repeat);
		Material material = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		material.setTexture("DiffuseMap", tex3);
		setMaterialOnSpatial(spatial, material);
	}


	private static void setMaterialOnSpatial(Spatial spatial, Material mat) {
		if (spatial instanceof Node) {
			Node node = (Node) spatial;
			for (Spatial s : node.getChildren()) {
				setMaterialOnSpatial(s, mat);
			}
		} else {
			Geometry g = (Geometry)spatial;
			g.setMaterial(mat);
		}
	}


	public static void scaleModelToHeight(Spatial model, float height) {
		BoundingBox bb = (BoundingBox)model.getWorldBound();
		float currHeight = bb.getYExtent() * 2;
		float frac = height/currHeight;
		model.scale(frac);
	}


	public static void scaleModelToWidth(Spatial model, float width) {
		BoundingBox bb = (BoundingBox)model.getWorldBound();
		float currWidth = bb.getXExtent() * 2;
		float frac = width/currWidth;
		model.scale(frac);
	}


	public static void moveYOriginTo(Spatial model, float yPos) {
		BoundingBox bb = (BoundingBox)model.getWorldBound();
		float currOrigin = bb.getCenter().y - bb.getYExtent();
		model.move(new Vector3f(0, yPos - currOrigin, 0));

	}


	public static AnimControl getNodeWithControls(Node s) {
		AnimControl control = null;
		int ch = s.getChildren().size();
		for (int i=0 ; i<ch ; i++) {
			Spatial sp = s.getChild(i);
			if (sp instanceof Node) {
				Node n2 = (Node)sp;
				if (n2.getNumControls() > 0) {
					control = n2.getControl(AnimControl.class);
					if (control != null) {
						return control;
					}
				} else {
					return getNodeWithControls((Node)sp);
				}
			}
		}
		return null;
	}


}
