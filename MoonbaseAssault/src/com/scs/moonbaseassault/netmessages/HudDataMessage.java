package com.scs.moonbaseassault.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

@Serializable
public class HudDataMessage extends MyAbstractMessage {
	
	public int scannerData[][];
	public int compsDestroyed;
	
	public HudDataMessage() {
		super(true, true);
	}
	

	public HudDataMessage(int _scannerData[][], int _compsDestroyed) {
		this();
		
		scannerData = _scannerData;
		compsDestroyed = _compsDestroyed;
	}
	

}
