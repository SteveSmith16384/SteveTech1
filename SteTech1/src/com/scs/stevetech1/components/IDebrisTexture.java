package com.scs.stevetech1.components;

/*
 * For when something is hit by a bullet, this determines what texture to give to the
 * explosion shards.
 * 
 */
public interface IDebrisTexture {

	String getDebrisTexture();
	
	float getMinDebrisSize();

	float getMaxDebrisSize();
}
