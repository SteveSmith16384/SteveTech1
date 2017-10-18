package com.scs.stetech1.server;

import java.io.IOException;
import ssmith.util.MyProperties;

public class SorcerersProperties extends MyProperties {

	public SorcerersProperties(String file) throws IOException {
		super(file);
	}
	

	public float GetGamepadUpDownAdjust() {
		return super.getPropertyAsFloat("GamepadUpDownAdjust", .4f);
	}


	public float GetGamepadMoveSpeed() {
		return super.getPropertyAsFloat("GamepadMoveSpeed", 5f);
	}


	public float GetGamepadTurnSpeed() {
		return super.getPropertyAsFloat("GamepadTurnSpeed", 100f);
	}


	public float GetGamepadDeadZone() {
		return super.getPropertyAsFloat("GamepadDeadZone", 0.0015f);
	}


	public float GetMaxTurnSpeed() {
		return super.getPropertyAsFloat("MaxTurnSpeed", 1f);
	}
	
	
	public float GetBaseScoreInc() {
		return super.getPropertyAsFloat("BaseScoreInc", 0.005f);
	}

	
	public float GetRestartTimeSecs() {
		return super.getPropertyAsFloat("RestartTimeSecs", 3f);
	}
	
	
	public float GetInvulnerableTimeSecs() {
		return super.getPropertyAsFloat("InvulnerableTimeSecs", 3f);
		
	}

}
