package com.scs.stetech1.data;

public class SimplePlayerData { // POJO

	public int id;
	public String playerName;
	public int side;
	
	public SimplePlayerData(int _id, String name, int _side) {
		super();
		
		this.playerName = name;
		id = _id;
		side = _side;
	}
}
