package com.scs.stetech1.hud;

import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.scs.stetech1.client.GenericClient;
import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.shared.IEntityController;

public class AbstractHUDImage extends Picture implements IProcessByClient {

	private IEntityController game;
	private float timeLeft;

	public AbstractHUDImage(IEntityController _game, Node guiNode, String tex, float w, float h, float dur) {
		super("AbstractHUDImage");

		game = _game;
		this.timeLeft = dur;

		setImage(game.getAssetManager(), tex, true);
		setWidth(w);
		setHeight(h);
		//this.setPosition(w/2, h/2);

		guiNode.attachChild(this);
		//todo ? game.addEntity(this);

	}


	@Override
	public void process(GenericClient client, float tpf) {
		if (timeLeft > 0) {
			this.timeLeft -= tpf;
			if (this.timeLeft <= 0) {
				this.removeFromParent();
				//todo game.removeEntity(this);
			}
		}
	}

}
