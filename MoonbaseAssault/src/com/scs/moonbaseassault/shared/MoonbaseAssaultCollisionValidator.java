package com.scs.moonbaseassault.shared;

import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.stevetech1.components.IDontCollideWithComrades;
import com.scs.stevetech1.components.ILaunchable;
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

		// Explosion shards don't collide with player
		if ((pa.type == Globals.BULLET_EXPLOSION_EFFECT && pb.type == MoonbaseAssaultClientEntityCreator.SOLDIER_AVATAR) || (pb.type == Globals.BULLET_EXPLOSION_EFFECT && pa.type == MoonbaseAssaultClientEntityCreator.SOLDIER_AVATAR)) {
			return false;
		}

		// Explosion shards don't collide with AI
		if ((pa.type == Globals.BULLET_EXPLOSION_EFFECT && pb.type == MoonbaseAssaultClientEntityCreator.AI_SOLDIER) || (pb.type == Globals.BULLET_EXPLOSION_EFFECT && pa.type == MoonbaseAssaultClientEntityCreator.AI_SOLDIER)) {
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
		
		// Anything with a side don't collide if on same side
		if (pa instanceof IDontCollideWithComrades && pb instanceof IDontCollideWithComrades) {
			// units on the same side don't collide
			IDontCollideWithComrades aa = (IDontCollideWithComrades)pa;
			IDontCollideWithComrades ab = (IDontCollideWithComrades)pb;
			if (aa.getSide() == ab.getSide()) {
				return false;
			}
		}
/*		
		// Prevent bullets getting hit by the shooter
		if (pa instanceof ILaunchable) {
			ILaunchable aa = (ILaunchable)pa;
			if (aa.getLauncher() == pb) {
				return false;
			}
		}
		if (pb instanceof ILaunchable) {
			ILaunchable ab = (ILaunchable)pb;
			if (ab.getLauncher() == pa) {
				return false;
			}
		}
*/

		return super.canCollide(pa, pb);

	}

}
