package com.scs.stevetech1.components;

import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.scs.stevetech1.hud.IHUD;

public interface IDrawOnHUD {

	Node getHUDItem();
	
	void drawOnHud(IHUD hud, Camera cam);
}
