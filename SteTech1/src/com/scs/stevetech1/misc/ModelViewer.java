package com.scs.stevetech1.misc;

import com.ding.effect.outline.filter.OutlinePreFilter;
import com.ding.effect.outline.filter.OutlineProFilter;
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
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;

public class ModelViewer extends SimpleApplication implements AnimEventListener {

	private AnimControl control;
	private FilterPostProcessor fpp;

	public static void main(String[] args) {
		ModelViewer app = new ModelViewer();
		app.showSettings = false;

		app.start();
	}


	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("../UndercoverAgent/assets/", FileLocator.class); // default

		cam.setFrustumPerspective(60, settings.getWidth() / settings.getHeight(), .1f, 100);

		super.getViewPort().setBackgroundColor(ColorRGBA.Black);

		setupLight();

		Spatial model = assetManager.loadModel("Models/Holiday/Snowman.obj");
		//Spatial model = (Node)assetManager.loadModel("test.blend");
		//JMEModelFunctions.setTextureOnSpatial(assetManager, model, SoldierTexture.getTexture());

		if (model instanceof Node) {
			control = this.getNodeWithControls((Node)model);
			if (control != null) {
				control.addListener(this);
				Globals.p("Animations: " + control.getAnimationNames());
				AnimChannel channel = control.createChannel();
				channel.setAnim("run");
			} else {
				Globals.p("No animation control");
			}
		}

		rootNode.attachChild(model);

		this.rootNode.attachChild(JMEModelFunctions.getGrid(assetManager, 10));

		rootNode.updateGeometricState();

		model.updateModelBound();
		BoundingBox bb = (BoundingBox)model.getWorldBound();
		Globals.p("Model w/h/d: " + (bb.getXExtent()*2) + "/" + (bb.getYExtent()*2) + "/" + (bb.getZExtent()*2));

		this.flyCam.setMoveSpeed(12f);
		
		fpp = new FilterPostProcessor(assetManager);
		viewPort.addProcessor(fpp);

		this.showOutlineEffect(model, 3, ColorRGBA.Red);//, this.getViewPort());

	}


	private AnimControl getNodeWithControls(Node s) {
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
		al.setColor(ColorRGBA.White.mult(1));
		rootNode.addLight(al);

		DirectionalLight dirlight = new DirectionalLight(); // FSR need this for textures to show
		dirlight.setColor(ColorRGBA.White.mult(1.5f));
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


	public void showOutlineEffect(Spatial model, int width, ColorRGBA color) {
		OutlineProFilter outlineFilter = model.getUserData("OutlineProFilter");
		if (outlineFilter == null) {
			ViewPort outlineViewport = renderManager.createPreView("outlineViewport", cam);
			FilterPostProcessor outlinefpp = new FilterPostProcessor(assetManager);
			OutlinePreFilter outlinePreFilter = new OutlinePreFilter();
			outlinefpp.addFilter(outlinePreFilter);
			outlineViewport.attachScene(model);
			outlineViewport.addProcessor(outlinefpp);

			outlineViewport.setClearFlags(true, false, false);
			outlineViewport.setBackgroundColor(new ColorRGBA(0f, 0f, 0f, 0f));
			
			outlineFilter = new OutlineProFilter(outlinePreFilter);
			model.setUserData("OutlineProFilter", outlineFilter);
			outlineFilter.setOutlineColor(color);
			outlineFilter.setOutlineWidth(width);
			fpp.addFilter(outlineFilter);
		} else {
			outlineFilter.setEnabled(true);
			outlineFilter.getOutlinePreFilter().setEnabled(true);
		}
	}


	public void hideOutlineEffect(Spatial model) {
		OutlineProFilter outlineFilter = model.getUserData("OutlineProFilter");
		if (outlineFilter != null) {
			outlineFilter.setEnabled(false);
			outlineFilter.getOutlinePreFilter().setEnabled(false);
		}
	}

}