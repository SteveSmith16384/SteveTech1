package com.scs.stevetech1.shared;

public class HistoricalAnimationData implements ITimeStamped {

	public long timeStamp;
	public int animationCode;
	
	public HistoricalAnimationData(long _timeStamp, int animCode) {
		timeStamp = _timeStamp;
		animationCode = animCode;
	}

	
	@Override
	public long getTimestamp() {
		return timeStamp;
	}

}
