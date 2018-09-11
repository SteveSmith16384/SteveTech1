package com.scs.stevetech1.shared;

import com.scs.stevetech1.components.IDontCollideWithComrades;
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
		
		if (!pa.collideable || !pb.collideable) {
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

		return true;
	}

}
