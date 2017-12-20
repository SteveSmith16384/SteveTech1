package com.scs.stetech1.hud;

import java.util.HashMap;

import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.scs.stetech1.client.AbstractGameClient;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.shared.IEntityController;

public class AbstractHUDImage extends Picture implements IEntity, IProcessByClient {

	private AbstractGameClient game;
	private float timeLeft;
	private int id;

	public AbstractHUDImage(AbstractGameClient _game, int _id, Node guiNode, String tex, float w, float h, float dur) {
		super("AbstractHUDImage");

		id = _id;
		game = _game;
		this.timeLeft = dur;

		setImage(game.getAssetManager(), tex, true);
		setWidth(w);
		setHeight(h);
		//this.setPosition(w/2, h/2);

		guiNode.attachChild(this);
		game.addClientOnlyEntity(this);

	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf) {
		if (timeLeft > 0) {
			this.timeLeft -= tpf;
			if (this.timeLeft <= 0) {
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
		game.removeClientOnlyEntity(this);
		
	}

}
