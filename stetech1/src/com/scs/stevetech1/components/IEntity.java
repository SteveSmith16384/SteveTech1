package com.scs.stevetech1.components;

import java.util.HashMap;

public interface IEntity {

	//boolean isClientSideOnly();
	
	boolean requiresProcessing();
	
	String getName();
	
	int getID();
	
	int getGameID();
	
	int getType();
	
	HashMap<String, Object> getCreationData();
	
	void remove();
	
	boolean hasNotBeenRemoved();
	
}
