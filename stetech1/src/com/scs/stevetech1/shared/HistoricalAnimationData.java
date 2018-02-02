package com.scs.stevetech1.shared;

public class HistoricalAnimationData implements ITimeStamped {

	public long timeStamp;
	public Object animation;
	
	public HistoricalAnimationData(long _timeStamp, Object anim) {
		timeStamp = _timeStamp;
		animation = anim;
	}

	
	@Override
	public long getTimestamp() {
		return timeStamp;
	}

}
