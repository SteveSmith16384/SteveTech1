package com.scs.stevetech1.hud;

import java.util.HashMap;

import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.server.Globals;

public class AbstractHUDImage extends Picture implements IEntity, IProcessByClient {

	private AbstractGameClient game;
	private float timeLeftSecs;
	private int id;

	public AbstractHUDImage(AbstractGameClient _game, int _id, Node guiNode, String tex, int x, int y, int w, int h, float durSecs) {
		super("AbstractHUDImage");

		id = _id;
		game = _game;
		this.timeLeftSecs = durSecs;

		setImage(game.getAssetManager(), tex, true);
		setWidth(w);
		setHeight(h);
		this.setPosition(x, y);//w/2, h/5);

		guiNode.attachChild(this);
		game.addEntity(this);
		
		if (Globals.DEBUG_MISSING_WON_MSG) {
			Globals.p("Showing hud image " + tex);
		}


	}


	@Override
	public void processByClient(IClientApp client, float tpf) {
		if (timeLeftSecs > 0) {
			this.timeLeftSecs -= tpf;
			if (this.timeLeftSecs <= 0) {
				this.remove();
			}
		}
	}


	@Override
	public int getID() {
		return id;
	}


	@Override
	public int getType() {
		return -1;
	}


	@Override
	public HashMap<String, Object> getCreationData() {
		return null;
	}


	@Override
	public void remove() {
		this.removeFromParent();
		game.removeEntity(this.getID());
		
	}


	@Override
	public boolean requiresProcessing() {
		return true;
	}


	@Override
	public int getGameID() {
		return 0;
	}


	@Override
	public boolean hasNotBeenRemoved() {
		return this.parent != null;
	}

}
