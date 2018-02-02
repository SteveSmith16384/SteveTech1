package com.scs.stevetech1.components;

public interface IRequiresAmmoCache<T> { // Used by system UpdateAmmoCacheSystem - todo - need T?

	int getID();
	
	boolean requiresAmmo();
	
	public int getAmmoType();
	
	void addToCache(T o);

}
