package com.scs.moonbaseassault.shared;

import com.scs.stevetech1.data.SimpleGameData;

public class MoonbaseAssaultGameData extends SimpleGameData {

	public int computersDestroyed = 0;

	public MoonbaseAssaultGameData() {
		// Serialization
	}


	public MoonbaseAssaultGameData(int gameID) {
		super(gameID);
	}
}
