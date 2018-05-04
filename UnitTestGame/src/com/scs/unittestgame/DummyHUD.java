package com.scs.unittestgame;

import com.jme3.scene.Node;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.hud.IHUD;

public class DummyHUD extends Node implements IHUD {

	@Override
	public Node getRootNode() {
		return new Node();
	}

	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		
	}

	@Override
	public void setDebugText(String s) {
		
	}

	@Override
	public void showDamageBox() {
		
	}

	@Override
	public void showMessage(String s) {
		
	}

	@Override
	public void addItem(Node n) {
		
	}

}
