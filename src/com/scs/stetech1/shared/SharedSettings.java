package com.scs.stetech1.shared;

public class SharedSettings {
	
	public static final boolean DEBUG = true;
	public static final int PORT = 6143;
	public static final int SEND_INPUT_INTERVAL_MS = 50;
	public static final int SERVER_SEND_UPDATE_INTERVAL_MS = 100;
	public static final int PING_INTERVAL_MS = 100 * 1000;
	
	private SharedSettings() {
		// 
	}

	
	public static void p(String s)
	{
		System.out.println(s);
	}
	

}
