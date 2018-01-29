package com.scs.undercoveragent;

import java.io.IOException;
import ssmith.util.MyProperties;

public class UndercoverAgentGameProperties extends MyProperties { // todo - use it

	public UndercoverAgentGameProperties(String file) throws IOException {
		super(file);
	}
	

	public float GetRestartTimeSecs() {
		return super.getPropertyAsFloat("RestartTimeSecs", 3f);
	}
	
	
	public float GetInvulnerableTimeSecs() {
		return super.getPropertyAsFloat("InvulnerableTimeSecs", 3f);
		
	}

}
