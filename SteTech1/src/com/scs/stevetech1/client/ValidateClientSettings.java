package com.scs.stevetech1.client;

public class ValidateClientSettings {

	public String gameCode; // To check the right type of client is connecting
	public String key; // Check we're a valid client
	public double clientVersion; // Check we're up to date enough
	
	public ValidateClientSettings(String _gameCode, String _key, double _clientVersion) {
		gameCode =_gameCode;
		key = _key;
		clientVersion = _clientVersion;
	}

}
