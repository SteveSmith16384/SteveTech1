package com.scs.stevetech1.netmessages;

import com.jme3.math.Vector3f;

public class PlaySoundMessage {

	public String sound;
	public Vector3f pos;
	
	public PlaySoundMessage() {
		super(true);
	}
	
	
	public PlaySoundMessage(String _sound, Vector3f _pos) {
		this();
		
		sound = _sound;
		pos = _pos;
	}
}
