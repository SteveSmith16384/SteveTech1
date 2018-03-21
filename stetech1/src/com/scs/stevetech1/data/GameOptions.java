package com.scs.stevetech1.data;

public class GameOptions {

	public String displayName;
	public long deployDurationMillis = 10 * 1000;
	public long gameDurationMillis = 60 * 1000;
	public long finishedDurationMillis = 5 * 1000;
	public String ourExternalIP, lobbyip;
	public int ourExternalPort, lobbyport;
	public float avatarRestartTimeSecs, avatarInvulnDurationSecs;

	public GameOptions(String _displayName,  
			long _deployDuration, long _gameDuration, long _finishedDuration,
			String _ourExternalIP, int _ourExternalPort, String _lobbyip, int _lobbyport,
			float _restartTime, float _invulnDuration) {
		
		displayName =_displayName;
		
		deployDurationMillis = _deployDuration;
		gameDurationMillis = _gameDuration;
		finishedDurationMillis = _finishedDuration;
		
		ourExternalIP = _ourExternalIP;
		ourExternalPort = _ourExternalPort;
		lobbyip = _lobbyip;
		lobbyport =_lobbyport;
		
		avatarRestartTimeSecs = _restartTime;
		avatarInvulnDurationSecs = _invulnDuration;
	}
	
}
