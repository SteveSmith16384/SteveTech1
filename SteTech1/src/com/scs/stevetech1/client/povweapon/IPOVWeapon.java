package com.scs.stevetech1.client.povweapon;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public interface IPOVWeapon {

	void update(float tpfSecs);
	
	void startReloading(float durationSecs);
	
	void show(Node node);
	
	void hide();
	
	Vector3f getPOVBulletStartPos_Clone();
}
