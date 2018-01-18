package com.scs.stevetech1.misc;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.jme.JMEFunctions;
import com.scs.stevetech1.server.Globals;

public class ModelViewer extends SimpleApplication implements AnimEventListener {

	private AnimControl control;

	public static void main(String[] args){
		ModelViewer app = new ModelViewer();
		app.showSettings = false;

		app.start();
	}


	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default
		//assetManager.registerLocator("assets/Textures/", FileLocator.class);

		super.getViewPort().setBackgroundColor(ColorRGBA.Black);

		cam.setFrustumPerspective(60, settings.getWidth() / settings.getHeight(), .1f, 100);

		setupLight();

		/*
		Spatial model = assetManager.loadModel("Models/zombie/Zombie.blend");
		model.scale(.125f);
		JMEFunctions.SetTextureOnSpatial(assetManager, model, "Models/zombie/ZombieTexture.png");
		 */

		/*
		Spatial model = assetManager.loadModel("Models/3d-character/environment/house/model.blend");
		model.scale(.4f);
		 */

		//Spatial model = assetManager.loadModel("Models/3d-character/environment/wood-barrier/model.blend"); // Fence post
		//model.scale(1);

		//Spatial model = assetManager.loadModel("Models/3d-character/environment/wall/model.blend"); // Has a bend in it?
		//model.scale(1);

		//Spatial model = assetManager.loadModel("Models/3d-character/environment/floor/model.blend"); // Funny shaped floor
		//JMEFunctions.SetTextureOnSpatial(assetManager, model, "Models/3d-character/environment/floor/texture.png");
		//model.scale(1);

		//Spatial model = assetManager.loadModel("Models/3d-character/environment/grass/modelgrass2.blend");
		//JMEFunctions.SetTextureOnSpatial(assetManager, model, "Models/3d-character/environment/grass/textureGrass1.png");
		//model.scale(.1f);

		//Spatial model = assetManager.loadModel("Models/3d-character/environment/pole/model.blend");
		//model.scale(.1f);

		/*Spatial model = assetManager.loadModel("Models/3d-character/environment/trash-can/model.blend");
		JMEFunctions.SetTextureOnSpatial(assetManager, model, "Models/3d-character/environment/trash-can/texture.png");
		model.scale(1f);*/

		//Spatial model = assetManager.loadModel("Models/3d-vehicles/car/car.blend"); // Good

		/*Spatial model = assetManager.loadModel("Models/Pleasant Park Pack/Props/Tree 1/tree1.obj");
		JMEFunctions.SetTextureOnSpatial(assetManager, model, "Models/Pleasant Park Pack/Props/Tree 1/Materials/tree1.png");
		model.scale(.5f);*/

		Spatial model = assetManager.loadModel("Models/Steve.blend");
		model.scale(.2f);
		
		if (model instanceof Node) {
			Node s2 = this.getNodeWithControls((Node)model);
			if (s2 != null) {
				control = s2.getControl(AnimControl.class);
				if (control != null) {
					control.addListener(this);
					AnimChannel channel = control.createChannel();
					//channel.setAnim("WALK");
				} else {
					Globals.p("No animation control");
				}
			}
		}

		model.setModelBound(new BoundingBox());
		rootNode.attachChild(model);

		this.rootNode.attachChild(JMEFunctions.GetGrid(assetManager, 10));

		rootNode.updateGeometricState();

		model.updateModelBound();
		BoundingBox bb = (BoundingBox)model.getWorldBound();
		Globals.p("Model w/h/d: " + (bb.getXExtent()*2) + "/" + (bb.getYExtent()*2) + "/" + (bb.getZExtent()*2));

		this.flyCam.setMoveSpeed(12f);

	}


	private Node getNodeWithControls(Node s) {
		int ch = s.getChildren().size();
		for (int i=0 ; i<ch ; i++) {
			Spatial sp = s.getChild(i);
			if (sp instanceof Node) {
				if (sp.getNumControls() > 0) {
					return (Node)sp;
				} else {
					return this.getNodeWithControls((Node)sp);
				}
			}
		}
		return null;
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
		al.setColor(ColorRGBA.White.mult(1f));
		rootNode.addLight(al);

		DirectionalLight dirlight = new DirectionalLight(); // FSR need this for textures to show
		rootNode.addLight(dirlight);

	}


	@Override
	public void simpleUpdate(float tpf) {
		//System.out.println("Pos: " + this.cam.getLocation());
		//this.rootNode.rotate(0,  tpf,  tpf);

		//Globals.p("Model w/h/d: " + (bb.getXExtent()*2) + "/" + (bb.getYExtent()*2) + "/" + (bb.getZExtent()*2));
	}


	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {

	}


	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {

	}


}