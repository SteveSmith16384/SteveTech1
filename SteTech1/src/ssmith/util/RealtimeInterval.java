package ssmith.util;

public class RealtimeInterval {

	private long next_check_time, durationMS;

	public RealtimeInterval(long _duration) {
		this(_duration, false);
	}
	
	
	public RealtimeInterval(long _duration, boolean fire_now) {
		super();
		this.durationMS = _duration;
		if (fire_now) {
			this.next_check_time = System.currentTimeMillis(); // Fire straight away
		} else {
			this.next_check_time = System.currentTimeMillis() + durationMS;
		}
	}
	
	
	public void restartTimer() {
		this.next_check_time = System.currentTimeMillis() + durationMS;
	}

	
	public void setInterval(long amt, boolean restart) {
		durationMS = amt;
		
		if (restart) {
			this.restartTimer();
		}
	}

	
	public boolean hitInterval() {
		if (System.currentTimeMillis() >= this.next_check_time) {
			this.restartTimer();
			return true;
		}
		return false;
	}
	
	
	public void fireInterval() {
		this.next_check_time = System.currentTimeMillis(); // Fire straight away
	}
	
	
	public long getInterval() {
		return this.durationMS;
	}

}
