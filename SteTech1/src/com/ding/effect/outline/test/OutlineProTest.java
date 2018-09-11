/*
 * Copyright (c) 2009-2012 jMonkeyEngine All rights reserved. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of 'jMonkeyEngine' nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ding.effect.outline.test;

import com.ding.effect.outline.filter.OutlinePreFilter;
import com.ding.effect.outline.filter.OutlineProFilter;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 渐变的外描边(更耗性能)
 * @author DING
 */
public class OutlineProTest extends SimpleApplication implements ActionListener {

	public static void main(String[] args) {
		OutlineProTest app = new OutlineProTest();
		app.start();
	}

	private FilterPostProcessor fpp;
	private Node sceneModel;

	@Override
	public void simpleInitApp() {
		flyCam.setMoveSpeed(5f);
		// light
		AmbientLight al = new AmbientLight();
		rootNode.addLight(al);
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-1, -4, -2).normalizeLocal());
		sun.setColor(ColorRGBA.White);
		rootNode.addLight(sun);
		// model
		sceneModel = (Node) assetManager.loadModel("test.blend");
		rootNode.attachChild(sceneModel);
		// shader
		fpp = new FilterPostProcessor(assetManager);
		viewPort.addProcessor(fpp);

		showOutlineEffect(sceneModel.getChild("Cylinder"), 8, ColorRGBA.Yellow);
		showOutlineEffect(sceneModel.getChild("Cube"), 8, ColorRGBA.Cyan);
		showOutlineEffect(sceneModel.getChild("Sphere"), 8, ColorRGBA.Red);
		// input
		inputManager.addMapping("1", new KeyTrigger(KeyInput.KEY_1));
		inputManager.addMapping("2", new KeyTrigger(KeyInput.KEY_2));
		inputManager.addListener(this, "1", "2");
	}

	private void hideOutlineEffect(Spatial model) {
		OutlineProFilter outlineFilter = model.getUserData("OutlineProFilter");
		if (outlineFilter != null) {
			outlineFilter.setEnabled(false);
			outlineFilter.getOutlinePreFilter().setEnabled(false);
		}
	}

	private void showOutlineEffect(Spatial model, int width, ColorRGBA color) {
		OutlineProFilter outlineFilter = model.getUserData("OutlineProFilter");
		OutlinePreFilter outlinePreFilter;
		if (outlineFilter == null) {
			ViewPort outlineViewport = renderManager.createPreView("outlineViewport", cam);
			FilterPostProcessor outlinefpp = new FilterPostProcessor(assetManager);
			outlinePreFilter = new OutlinePreFilter();
			outlinefpp.addFilter(outlinePreFilter);
			outlineViewport.attachScene(model);
			outlineViewport.addProcessor(outlinefpp);

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

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if (isPressed) {
			if ("1".equals(name)) {
				hideOutlineEffect(sceneModel.getChild("Cylinder"));
			} else if ("2".equals(name)) {
				hideOutlineEffect(sceneModel.getChild("Cube"));
			}
		}
	}
}
