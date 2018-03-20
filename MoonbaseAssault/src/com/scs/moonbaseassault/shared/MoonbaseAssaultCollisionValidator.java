package com.scs.moonbaseassault.shared;

import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

public class MoonbaseAssaultCollisionValidator extends AbstractCollisionValidator {

	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		// Sliding doors shouldn't collide with floor/ceiling
		if ((pa.type == MoonbaseAssaultClientEntityCreator.FLOOR && pb.type == MoonbaseAssaultClientEntityCreator.DOOR) || pa.type == MoonbaseAssaultClientEntityCreator.DOOR && pb.type == MoonbaseAssaultClientEntityCreator.FLOOR) {
			return false;
		}
		// Sliding doors shouldn't collide with wall
		if ((pa.type == MoonbaseAssaultClientEntityCreator.WALL && pb.type == MoonbaseAssaultClientEntityCreator.DOOR) || pa.type == MoonbaseAssaultClientEntityCreator.DOOR && pb.type == MoonbaseAssaultClientEntityCreator.WALL) {
			return false;
		}
		return super.canCollide(a, b);

	}

}
