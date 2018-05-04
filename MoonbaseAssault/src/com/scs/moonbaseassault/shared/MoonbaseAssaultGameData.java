package com.scs.moonbaseassault.shared;

import com.scs.stevetech1.data.SimpleGameData;

public class MoonbaseAssaultGameData extends SimpleGameData {

	public int[] pointsForSide = new int[3]; // side 1 or 2

	public MoonbaseAssaultGameData() {
		// Serialization
	}


	public MoonbaseAssaultGameData(int gameID) {
		super(gameID);
	}
}
