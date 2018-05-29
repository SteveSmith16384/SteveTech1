package com.scs.undercoveragent;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.data.SimplePlayerData;

@Serializable
public class UASimplePlayerData extends SimplePlayerData {

	public int score;
	
	public UASimplePlayerData() {
		super();
	}
}
