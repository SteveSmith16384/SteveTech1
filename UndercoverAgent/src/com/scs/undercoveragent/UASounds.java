package com.scs.undercoveragent;

import com.scs.stevetech1.server.Globals;

public class UASounds {

	public static final int THROW = 1;
	public static final int SPLAT = 2;
	public static final int DIED = 3;
	public static final int FALL = 4;
	public static final int FOOTSTEPS = 5;
	public static final int START = 6;
	public static final int WINNER = 7;
	public static final int LOSER = 8;

	public static String getSoundFile(int id) {
		switch (id) {
		case THROW: return "Sounds/throw.wav";
		case SPLAT: return "Sounds/splat.wav";
		case DIED: return "Sounds/died.wav";
		case FALL: return "Sounds/fallfar.wav";
		case FOOTSTEPS: return "Sounds/footstep.wav";
		case START: return "Sounds/teleport.wav";
		case WINNER: return "Sounds/winner.wav";
		case LOSER: return "Sounds/loser.wav";
		default:
			if (!Globals.RELEASE_MODE) {
				throw new IllegalArgumentException("Unknown sound id:" + id);
			}
		}
		return null;
	}
	
}
