package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

import ssmith.lang.NumberFunctions;

/*
 * The origin for this should be left/bottom/front
 *
 */
public class MountainMapBorder extends PhysicalEntity {

	public MountainMapBorder(IEntityController _game, int id, float x, float y, float z, float size, Vector3f dir) {
		super(_game, id, UndercoverAgentClientEntityCreator.MOUNTAIN_MAP_BORDER, "MountainMapBorder", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", size);
			creationData.put("dir", dir);
		}

		if (!_game.isServer()) { // Not running in server
			Node container = new Node("MountainContainer");
			// Add mountain models
			for (float i=-(InvisibleMapBorder.BORDER_WIDTH/2) ; i<size+1 ; i+=InvisibleMapBorder.BORDER_WIDTH/2) { 
				Spatial model = game.getAssetManager().loadModel("Models/Holiday/Terrain2.blend");
					model.setShadowMode(ShadowMode.CastAndReceive);
				JMEModelFunctions.setTextureOnSpatial(game.getAssetManager(), model, "Textures/snow.jpg");
				JMEModelFunctions.scaleModelToWidth(model, InvisibleMapBorder.BORDER_WIDTH+1); // Since we rotate it, needs to be slightly wider
				model.setLocalTranslation(-InvisibleMapBorder.BORDER_WIDTH/2, 0, i);//-(InvisibleMapBorder.BORDER_WIDTH/2));
				JMEAngleFunctions.rotateToDirection(model, NumberFunctions.rnd(0, 359));
				container.attachChild(model);
			}
			mainNode.attachChild(container);
			JMEAngleFunctions.rotateToDirection(mainNode, dir);
		} else {
			// Do nothing on server
		}

		mainNode.setLocalTranslation(x, y, z);
		
		// Note we don't create a SimpleRigidBody, since this doesn't collide

	}

}
