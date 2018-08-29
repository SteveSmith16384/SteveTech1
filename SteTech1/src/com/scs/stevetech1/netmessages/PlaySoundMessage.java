package com.scs.stevetech1.netmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

@Serializable
public class PlaySoundMessage extends MyAbstractMessage {

	public int soundId, entityId;
	public Vector3f pos;
	public float volume;
	public boolean stream;
	
	public PlaySoundMessage() {
		super(true, true);
	}
	
	
	public PlaySoundMessage(int _soundId, int _entityId, Vector3f _pos, float _volume, boolean _stream) {
		this();
		
		soundId = _soundId;
		entityId = _entityId;
		pos = _pos;
		volume = _volume;
		stream = _stream;
	}
}
