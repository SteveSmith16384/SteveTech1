package com.scs.stevetech1.shared;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;

/*
 * This is so the client and the server can handle entities.
 */
public interface IEntityController {

	int getGameID();
	
	boolean isServer();

	void addEntity(IEntity e);
	
	void removeEntity(int id);
	
	AssetManager getAssetManager();
	
	RenderManager getRenderManager();
	
	SimplePhysicsController<PhysicalEntity> getPhysicsController();
	
	Node getGameNode();
	
	int getNextEntityID();
	
	public <V> Future<V> enqueue(Callable<V> callable);
	
	int getNumEntities();
	
	boolean canCollide(PhysicalEntity a, PhysicalEntity b);
	
	void playSound(int _soundId, int entityId, Vector3f _pos, float _volume, boolean _stream);
	
}
