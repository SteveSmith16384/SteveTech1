package com.scs.stevetech1.client;

public class ValidateClientSettings {

	public String gameCode; // To check the right type of client is connecting
	public String key; // Check we're a valid client
	public double clientVersion; // Check we're up to date enough
	
	/**
	 * 
	 * @param _gameCode - Must be the same as the server, to ensure the client is connecting to the same game.
	 * @param _clientVersion - Checked by the server to ensure the client is up to date.
	 * @param _key - A secret code to ensure the client is valid.
	 */
	public ValidateClientSettings(String _gameCode, double _clientVersion, String _key) {
		super();
		
		gameCode =_gameCode;
		clientVersion = _clientVersion;
		key = _key;
	}

}
