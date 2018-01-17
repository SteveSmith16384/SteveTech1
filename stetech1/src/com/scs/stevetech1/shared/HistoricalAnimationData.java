package com.scs.stevetech1.shared;

public class HistoricalAnimationData implements ITimeStamped {

	public long timeStamp;
	public String animation;
	
	public HistoricalAnimationData(long _timeStamp, String anim) {
		timeStamp = _timeStamp;
		animation = anim;
	}

	
	@Override
	public long getTimestamp() {
		return timeStamp;
	}

}
