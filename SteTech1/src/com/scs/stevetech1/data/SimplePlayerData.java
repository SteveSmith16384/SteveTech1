package com.scs.stevetech1.data;

import com.jme3.network.serializing.Serializable;

@Serializable
public class SimplePlayerData { // POJO

	public int id;
	public String playerName;
	public byte side;
	public long pingRTT;

	public SimplePlayerData() {
		// need empty constructor for deserialization
	}
	
	
	public SimplePlayerData(int _id, String name, byte _side) {
		super();
		
		this.playerName = name;
		id = _id;
		side = _side;
	}
}
