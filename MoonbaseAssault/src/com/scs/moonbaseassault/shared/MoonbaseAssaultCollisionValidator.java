package com.scs.moonbaseassault.shared;

import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

public class MoonbaseAssaultCollisionValidator extends AbstractCollisionValidator {

	@Override
	public boolean canCollide(PhysicalEntity pa, PhysicalEntity pb) {
		// Explosion shards don't collide with each other
		if (pa.type == Globals.BULLET_EXPLOSION_EFFECT && pb.type == Globals.BULLET_EXPLOSION_EFFECT) {
			return false;
		}

		// Sliding doors shouldn't collide with floor/ceiling
		if ((pa.type == MoonbaseAssaultClientEntityCreator.FLOOR && pb.type == MoonbaseAssaultClientEntityCreator.DOOR) || pa.type == MoonbaseAssaultClientEntityCreator.DOOR && pb.type == MoonbaseAssaultClientEntityCreator.FLOOR) {
			return false;
		}
		// Sliding doors shouldn't collide with wall
		if ((pa.type == MoonbaseAssaultClientEntityCreator.WALL && pb.type == MoonbaseAssaultClientEntityCreator.DOOR) || pa.type == MoonbaseAssaultClientEntityCreator.DOOR && pb.type == MoonbaseAssaultClientEntityCreator.WALL) {
			return false;
		}
		return super.canCollide(pa, pb);

	}

}
