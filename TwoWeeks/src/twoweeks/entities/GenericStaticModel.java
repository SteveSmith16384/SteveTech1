package twoweeks.entities;

import java.util.HashMap;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.Collidable;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class GenericStaticModel extends PhysicalEntity {

	public GenericStaticModel(IEntityController _game, int id, int type, String name, String modelFile, float height, String tex, float x, float y, float z, Quaternion q) {
		super(_game, id, type, name, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("name", name);
			creationData.put("modelFile", modelFile);
			creationData.put("height", height);
			creationData.put("tex", tex);
			creationData.put("q", q);
		}

		Spatial model = game.getAssetManager().loadModel(modelFile);
		if (tex != null) {
			JMEModelFunctions.setTextureOnSpatial( game.getAssetManager(), model, tex);
		}
		model.setShadowMode(ShadowMode.CastAndReceive);
		JMEModelFunctions.scaleModelToHeight(model, height);
		JMEModelFunctions.moveYOriginTo(model, 0f);
		// Autocentre
		/*model.updateModelBound();
		BoundingBox bv = (BoundingBox)model.getWorldBound();		
		model.move(bv.getXExtent(), 0, bv.getZExtent());*/
		
		//model.move(offset.x, 0, offset.z);
		//JMEAngleFunctions.rotateToDirection(model, new Vector3f(-1, 0, 0)); // Point model fwds

		this.mainNode.attachChild(model);
		
		mainNode.setLocalRotation(q);
		
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(2);
		simpleRigidBody.setNeverMoves(true);

		model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		//game.getRootNode().attachChild(this.mainNode);
		//game.addEntity(this);

	}

/*
	@Override
	public void processByServer(AbstractEntityServer server, float tpf) {
		super.processByServer(server, tpf);
	}
*/

	@Override
	public Collidable getCollidable() {
		return this.mainNode;
	}


}
