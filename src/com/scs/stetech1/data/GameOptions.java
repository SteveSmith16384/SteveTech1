package com.scs.stetech1.data;

public class GameOptions {

	private int maxPlayersPerSide;
	private int maxSides;
	private boolean allPlayersOnDifferentSides;

	public GameOptions(int _maxPlayersPerSide, int _maxSides) {
		maxPlayersPerSide = _maxPlayersPerSide;
		maxSides = _maxSides;
		allPlayersOnDifferentSides = (maxPlayersPerSide == 1);
	}

}
