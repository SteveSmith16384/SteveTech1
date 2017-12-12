package com.scs.stetech1.data;

public class GameOptions {

	private int maxPlayersPerSide;
	private int maxSides;
	private boolean allPlayersOnDifferentSides;
	public long deployDuration = 10 * 1000; // todo
	public long gameDuration = 60 * 1000; // todo
	public long finishedDuration = 5 * 1000; // todo
	public String ourExternalIPAddress, lobbyServerIPAddress;

	public GameOptions(int _maxPlayersPerSide, int _maxSides) {
		maxPlayersPerSide = _maxPlayersPerSide;
		maxSides = _maxSides;
		allPlayersOnDifferentSides = (maxPlayersPerSide == 1);
	}
	
	
	public boolean areAllPlayersOnDifferentSides() {
		return this.allPlayersOnDifferentSides;
	}

}
