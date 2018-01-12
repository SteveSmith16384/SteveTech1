package com.scs.stevetech1.shared;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;

public interface IEntityController {

	boolean isServer();

	void addEntity(IEntity e);
	
	void removeEntity(int id);
	
	JmeContext.Type getJmeContext();
	
	AssetManager getAssetManager();
	
	SimplePhysicsController<PhysicalEntity> getPhysicsController();
	
	Node getRootNode();
	
	int getNextEntityID();
	
}
