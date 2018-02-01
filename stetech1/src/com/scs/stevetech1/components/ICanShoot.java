package com.scs.stevetech1.components;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.server.RayCollisionData;

public interface ICanShoot { // todo - rename to IPlayerShooter or something

	// todo - are all these used?
	int getID();
	
	int getSide();
	
	Vector3f getShootDir();
	
	Vector3f getBulletStartPos();
	
	RayCollisionData checkForCollisions(Ray r, float range);
	
	void hasSuccessfullyHit(IEntity e);
	
}
