package com.scs.stevetech1.client;

import com.scs.stevetech1.components.IEntity;

public interface IClientApp {

	long getRenderTime();
	
	long getServerTime();
	
	IEntity getEntity(int id);
}
