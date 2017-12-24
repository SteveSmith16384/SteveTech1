package com.scs.stevetech1.data;

public class GameOptions {

	private int maxPlayersPerSide;
	private int maxSides;
	private boolean allPlayersOnDifferentSides;
	public long deployDuration = 10 * 1000;
	public long gameDuration = 60 * 1000;
	public long finishedDuration = 5 * 1000;
	public String ourExternalIPAddress, lobbyServerIPAddress;

	public GameOptions(int _maxPlayersPerSide, int _maxSides, long _deployDuration, long _gameDuration, long _finishedDuration) {
		maxPlayersPerSide = _maxPlayersPerSide;
		maxSides = _maxSides;
		allPlayersOnDifferentSides = (maxPlayersPerSide == 1);
		
		deployDuration = _deployDuration;
		gameDuration = _gameDuration;
		finishedDuration = _finishedDuration;
	}
	
	
	public boolean areAllPlayersOnDifferentSides() {
		return this.allPlayersOnDifferentSides;
	}

}
