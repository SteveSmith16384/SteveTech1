package com.scs.stetech1.components;

public interface IRequiresAmmoCache<T> {

	int getID();
	
	boolean requiresAmmo();
	
	public int getAmmoType();
	
	void addToCache(T o);
}
