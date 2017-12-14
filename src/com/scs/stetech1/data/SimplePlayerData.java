package com.scs.stetech1.data;

import com.jme3.network.serializing.Serializable;

@Serializable
public class SimplePlayerData { // POJO

	public int id;
	public String playerName;
	public int side;
	public long pingRTT;

	public SimplePlayerData() {
		// need empty constructor for deserialization
	}
	
	
	public SimplePlayerData(int _id, String name, int _side) {
		super();
		
		this.playerName = name;
		id = _id;
		side = _side;
	}
}
