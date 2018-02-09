package com.scs.stevetech1.shared;

public class HistoricalAnimationData implements ITimeStamped {

	public long timeStamp;
	public String animationCode;
	
	public HistoricalAnimationData(long _timeStamp, String animCode) {
		timeStamp = _timeStamp;
		animationCode = animCode;
	}

	
	@Override
	public long getTimestamp() {
		return timeStamp;
	}

}
