package com.scs.stetech1.data;

public class PlayerData {

	public int id;
	public String playerName;
	public byte side;
	
	public PlayerData(int _id, String name) {
		super();
		
		this.playerName = name;
		id = _id;
	}
}
