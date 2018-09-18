package com.scs.stevetech1.components;

import com.scs.stevetech1.entities.AbstractServerAvatar;

/**
 * Entities implementing this will only collide with players. 
 *
 */
public interface IPlayerCollectable {

	void collected(AbstractServerAvatar avatar);
}
