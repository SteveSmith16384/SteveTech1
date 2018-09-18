package com.scs.stevetech1.shared;

import com.scs.stevetech1.components.IDontCollideWithComrades;
import com.scs.stevetech1.components.IPlayerCollectable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;

/**
 * This determines whether two entities can collide.
 *
 */
public class AbstractCollisionValidator {

	/**
	 * Should always return true unless they can't collide
	 * @param pa
	 * @param pb
	 * @return
	 */
	public boolean canCollide(PhysicalEntity pa, PhysicalEntity pb) {
		if (pa.isMarkedForRemoval() || pb.isMarkedForRemoval()) {
			return false;
		}
		
		// Don't collide if one or both entities are not collidable
		if (!pa.collideable || !pb.collideable) {
			return false;
		}

		
		// Anything with a side don't collide if on same side.
		// This prevents bullets colliding with their shooter, or units of the same side blocking each other.
		if (pa instanceof IDontCollideWithComrades && pb instanceof IDontCollideWithComrades) {
			IDontCollideWithComrades aa = (IDontCollideWithComrades)pa;
			IDontCollideWithComrades ab = (IDontCollideWithComrades)pb;
			if (aa.getSide() == ab.getSide()) {
				return false;
			}
		}

		// Medipacks only collide with players
		if ((pa instanceof IPlayerCollectable && pb instanceof AbstractAvatar) || pa instanceof AbstractAvatar && pb instanceof IPlayerCollectable) {
			return false;
		}
		
		return true;
	}

}
