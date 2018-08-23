package com.scs.stevetech1.components;

import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public interface IDrawOnHUD {

	Node getHUDItem();
	
	void drawOnHud(Node node, Camera cam);
}
