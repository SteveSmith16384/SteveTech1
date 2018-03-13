package com.scs.moonbaseassault.netmessages;

import java.awt.Point;
import java.util.List;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

@Serializable
public class HudDataMessage extends MyAbstractMessage { // todo - send this periodically
	
	public int scannerData[][];
	public List<Point> units;
	
	public HudDataMessage() {
		super(true, true);
	}
	

	public HudDataMessage(int _scannerData[][], List<Point> _units) {
		this();
		
		scannerData = _scannerData;
		units = _units;
	}
	

}
