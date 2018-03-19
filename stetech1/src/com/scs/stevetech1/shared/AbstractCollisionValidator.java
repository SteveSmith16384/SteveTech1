package com.scs.stevetech1.shared;

import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;

public class AbstractCollisionValidator {

	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject;
		PhysicalEntity pb = b.userObject;

		if (!pa.collideable || !pb.collideable) {
			return false;
		}
		/*
		if (pa.canCollideWith(pb) == false || pb.canCollideWith(pa) == false) {
			return false;
		}
*/
		if (pa instanceof AbstractAvatar && pb instanceof AbstractAvatar) {
			// Avatars on the same side don't collide
			AbstractAvatar aa = (AbstractAvatar)pa;
			AbstractAvatar ab = (AbstractAvatar)pb;
			if (aa.side == ab.side) {
				return false;
			}
		}
		
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

		if (pa instanceof ILaunchable && pb instanceof ILaunchable) {
			// Bullets don't collide with each other
			return false;
		}

		return true;
	}

}
