package com.scs.stetech1.misc;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.scs.stetech1.jme.JMEFunctions;

public class ModelViewer extends SimpleApplication {

	public static void main(String[] args){
		ModelViewer app = new ModelViewer();
		app.showSettings = false;

		app.start();
	}


	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default
		//assetManager.registerLocator("assets/Textures/", FileLocator.class);

		super.getViewPort().setBackgroundColor(ColorRGBA.White);

		cam.setFrustumPerspective(60, settings.getWidth() / settings.getHeight(), .1f, 100);
		
		setupLight();

		//Spatial model = assetManager.loadModel("Models/AbstractRTSModels/Player.obj");
		Spatial model = assetManager.loadModel("Models/elvis/tinker.obj");
		//model.updateModelBound();
		model.scale(.1f);
		
		//JMEFunctions.SetTextureOnSpatial(assetManager, model, "Textures/sun.jpg");
		//JMEFunctions.SetTextureOnSpatial(assetManager, model, "Textures/cells3.png");
		//JMEFunctions.SetTextureOnSpatial(assetManager, model, "Textures/computerconsole2.jpg");

		
		model.setModelBound(new BoundingBox());
		model.updateModelBound();

		rootNode.attachChild(model);

		this.rootNode.attachChild(JMEFunctions.GetGrid(assetManager, 10));

		this.flyCam.setMoveSpeed(12f);

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