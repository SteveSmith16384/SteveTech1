package com.scs.stevetech1.components;

import com.scs.stevetech1.entities.PhysicalEntity;

/**
 * Entity will be notified if they have collided with something, so they can run their own code.
 * @author stephencs
 *
 */
public interface INotifiedOfCollision {

	public void notifiedOfCollision(PhysicalEntity collidedWith);
}
