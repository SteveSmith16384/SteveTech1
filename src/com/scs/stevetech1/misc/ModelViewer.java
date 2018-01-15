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

public class ModelViewer extends SimpleApplication implements AnimEventListener {

	private AnimChannel channel;
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
		model.setModelBound(new BoundingBox());
		model.updateModelBound();
		model.updateGeometricState();
		JMEFunctions.SetTextureOnSpatial(assetManager, model, "Models/zombie/ZombieTexture.png");
		 */

		Spatial model = assetManager.loadModel("Models/3d-character/character/character.blend");
		model.scale(5);
		
		//Spatial model = assetManager.loadModel("Models/western-fps-2d/background-elements/3D-table/table.blend");
		//model.scale(5);

		Node s2 = this.getNodeWithControls((Node)model);
		if (s2 != null) {
			control = s2.getControl(AnimControl.class);
			control.addListener(this);
			channel = control.createChannel();
			channel.setAnim("WALK");
		}

		rootNode.attachChild(model);

		this.rootNode.attachChild(JMEFunctions.GetGrid(assetManager, 10));

		this.flyCam.setMoveSpeed(12f);

		rootNode.updateGeometricState();

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
	}


	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {

	}


	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {

	}


}