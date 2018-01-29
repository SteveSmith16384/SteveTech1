package com.scs.stevetech1.components;

/*
 * For entities such as snowball bullets, grenades and laser bullets.
 * THIS ONLY APPLIES WHEN THE ENTITY HAS BEEN STARTED BY THE CLIENT, NOT ANY OTHER CLIENT!
 */
public interface IClientControlled {

	boolean isItOurEntity();
}
