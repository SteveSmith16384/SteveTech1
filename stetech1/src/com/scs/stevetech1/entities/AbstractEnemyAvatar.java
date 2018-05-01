package com.scs.stevetech1.entities;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

/*
 * This is only used client-side.
 */
public abstract class AbstractEnemyAvatar extends PhysicalEntity implements IAffectedByPhysics, IAnimatedClientSide, IProcessByClient, ISetRotation { 
	
	protected IAvatarModel anim;
	private Spatial avatarModel;
	
	public AbstractEnemyAvatar(IEntityController game, int type, int eid, float x, float y, float z, IAvatarModel _anim, int side) {
		super(game, eid, type, "EnemyAvatar", true);

		anim = _anim;
		
		// Create box for collisions
		//Box box = new Box(anim.getBoundingBox().getXExtent(), anim.getBoundingBox().getYExtent(), anim.getBoundingBox().getZExtent());
		Box box = new Box(anim.getSize().x/2, anim.getSize().y/2, anim.getSize().z/2);
		Geometry bbGeom = new Geometry("bbGeom_" + name, box);
		bbGeom.setLocalTranslation(0, anim.getSize().y/2, 0); // origin is centre!
		bbGeom.setCullHint(CullHint.Always); // Don't draw the collision box
		this.mainNode.attachChild(bbGeom);

		bbGeom.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		// Create model to look good
		avatarModel = anim.createAndGetModel(side);
		game.getGameNode().attachChild(avatarModel);

		this.setWorldTranslation(new Vector3f(x, y, z));

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this); // scs new - was NOT kinematic
		simpleRigidBody.setGravity(0); // So they move exactly where we want, even when client jumps

	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		// Set position and direction of avatar model, which doesn't get moved automatically
		this.avatarModel.setLocalTranslation(this.getWorldTranslation());
	}
	
	
	@Override
	public void remove() {
		super.remove();
		
		this.avatarModel.removeFromParent();
	}
	

	@Override
	public void setRotation(Vector3f dir) {
		Vector3f dir2 = new Vector3f(dir.x, 0, dir.z); 
		JMEAngleFunctions.rotateToDirection(avatarModel, dir2);
	}


}
