package com.scs.stevetech1.netmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

@Serializable
public class PlaySoundMessage extends MyAbstractMessage {

	public String sound;
	public Vector3f pos;
	public float volume;
	public boolean stream;
	
	public PlaySoundMessage() {
		super(true, true);
	}
	
	
	public PlaySoundMessage(String _sound, Vector3f _pos, float _volume, boolean _stream) {
		this();
		
		sound = _sound;
		pos = _pos;
		volume = _volume;
		stream = _stream;
	}
}