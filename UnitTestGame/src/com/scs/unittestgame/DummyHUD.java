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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDebugText(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showDamageBox() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showMessage(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addItem(Node n) {
		// TODO Auto-generated method stub
		
	}

}
