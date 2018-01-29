package com.scs.stevetech1.data;

public class GameOptions {

	public String displayName;
	private int maxPlayersPerSide;
	private int maxSides;
	private boolean allPlayersOnDifferentSides;
	public long deployDurationMillis = 10 * 1000;
	public long gameDurationMillis = 60 * 1000;
	public long finishedDurationMillis = 5 * 1000;
	public String ourExternalIP, lobbyip;
	public int ourExternalPort, lobbyport;
	public float restartTimeSecs, invulnDurationSecs;

	public GameOptions(String _displayName, int _maxPlayersPerSide, int _maxSides, long _deployDuration, long _gameDuration, long _finishedDuration,
			String _ourExternalIP, int _ourExternalPort, String _lobbyip, int _lobbyport,
			float _restartTime, float _invulnDuration) {
		
		displayName =_displayName;
		maxPlayersPerSide = _maxPlayersPerSide;
		maxSides = _maxSides;
		allPlayersOnDifferentSides = (maxPlayersPerSide == 1);
		
		deployDurationMillis = _deployDuration;
		gameDurationMillis = _gameDuration;
		finishedDurationMillis = _finishedDuration;
		
		ourExternalIP = _ourExternalIP;
		ourExternalPort = _ourExternalPort;
		lobbyip = _lobbyip;
		lobbyport =_lobbyport;
		
		restartTimeSecs = _restartTime;
		invulnDurationSecs = _invulnDuration;
	}
	
	
	public boolean areAllPlayersOnDifferentSides() {
		return this.allPlayersOnDifferentSides;
	}

}
