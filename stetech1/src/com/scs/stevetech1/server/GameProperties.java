package com.scs.stevetech1.server;

import java.io.IOException;
import ssmith.util.MyProperties;

public class GameProperties extends MyProperties { // todo - move to specific project

	public GameProperties(String file) throws IOException {
		super(file);
	}
	

	public float GetRestartTimeSecs() {
		return super.getPropertyAsFloat("RestartTimeSecs", 3f);
	}
	
	
	public float GetInvulnerableTimeSecs() {
		return super.getPropertyAsFloat("InvulnerableTimeSecs", 3f);
		
	}

}
