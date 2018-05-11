package com.scs.stevetech1.models;

import java.util.ArrayList;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;

public class BeamLaserModel extends Node {

	private static ArrayList<BeamLaserModel> spare = new ArrayList<BeamLaserModel>(); // Cache old models

	private Geometry g;
	private Cylinder cyl;

	public static BeamLaserModel Factory(AssetManager assetManager, Vector3f start, Vector3f end, ColorRGBA col, boolean generateMat, String tex, float diam, boolean cone) {
		if (spare.size() > 0) {
			BeamLaserModel m = spare.remove(0);
			m.setPoints(start, end, true);
			return  m;
		} else {
			return new BeamLaserModel(assetManager, start, end, col, generateMat, tex, diam, cone);
		}
	}

	private BeamLaserModel(AssetManager assetManager, Vector3f start, Vector3f end, ColorRGBA col, boolean generateMat, String tex, float diam, boolean cone) {
		super("Laser");

		
		cyl = new Cylinder(5, 10, diam, cone? 0: diam, start.distance(end), true, false);
		cyl.setBound(new BoundingBox());
		cyl.updateBound();
		g = new Geometry();
		g.setMesh(cyl);
		this.attachChild(g);

		if (generateMat) { // Only needed if client-side
			Material mat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			Texture t = assetManager.loadTexture(tex);
			mat.setTexture("DiffuseMap", t);
			mat.setColor("GlowColor", col);//ColorRGBA.Pink);
			mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			this.setQueueBucket(Bucket.Transparent);
			this.setMaterial(mat);
		}

		this.setPoints(start, end, false);

	}


	public void setPoints(Vector3f start, Vector3f end, boolean length_changed) {
		if (length_changed) {
			cyl.updateGeometry(5, 10, 0.03f, 0.03f, start.distance(end), true, false);
			cyl.updateBound();
			g.setMesh(cyl);
		}
		setLocalTranslation(FastMath.interpolateLinear(.5f, start, end));
		lookAt(end, Vector3f.UNIT_Y);

	}


	@Override
	public boolean removeFromParent() {
		this.spare.add(this);
		return super.removeFromParent();
	}

}
