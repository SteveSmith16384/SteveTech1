package com.scs.stevetech1.components;

public interface IEntityContainer<T> {

	int getID();

	void addToCache(T o);
	
	void removeFromCache(T o);

}
