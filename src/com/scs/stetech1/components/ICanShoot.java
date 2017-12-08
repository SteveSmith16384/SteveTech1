package com.scs.stetech1.components;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.stetech1.server.RayCollisionData;

public interface ICanShoot {

	int getSide();
	
	Vector3f getShootDir();
	
	Vector3f getBulletStartPos();
	
	RayCollisionData checkForCollisions(Ray r, float range);
	
	void hasSuccessfullyHit(IEntity e);
	
}
