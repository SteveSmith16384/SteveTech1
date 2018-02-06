package com.scs.stevetech1.components;

public interface IRequiresAmmoCache<T> { // todo - rename

	int getID();
/*	
	boolean requiresAmmo();
	
	public int getAmmoType();
	*/
	void addToCache(T o);

}
