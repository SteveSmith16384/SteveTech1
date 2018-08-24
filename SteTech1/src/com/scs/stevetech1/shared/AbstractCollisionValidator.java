package com.scs.stevetech1.shared;

import com.scs.stevetech1.components.IBullet;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;

/**
 * This determines whether two entities can collide.
 *
 */
public class AbstractCollisionValidator {

	public boolean canCollide(PhysicalEntity pa, PhysicalEntity pb) {
		if (!pa.collideable || !pb.collideable) {
			return false;
		}

		if (pa instanceof AbstractAvatar && pb instanceof AbstractAvatar) {
			// Avatars on the same side don't collide
			AbstractAvatar aa = (AbstractAvatar)pa;
			AbstractAvatar ab = (AbstractAvatar)pb;
			if (aa.side == ab.side) {
				return false;
			}
		}
		
		// Prevent bullets getting hit by the shooter
		if (pa instanceof IBullet) {
			IBullet aa = (IBullet)pa;
			if (aa.getLauncher() == pb) {
				return false;
			}
		}
		if (pb instanceof IBullet) {
			IBullet ab = (IBullet)pb;
			if (ab.getLauncher() == pa) {
				return false;
			}
		}

		if (pa instanceof IBullet && pb instanceof IBullet) {
			// Bullets don't collide with each other
			return false;
		}

		return true;
	}

}
