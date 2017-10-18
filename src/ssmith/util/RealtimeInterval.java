package ssmith.util;

public class RealtimeInterval {

	private long next_check_time, duration;

	public RealtimeInterval(long duration) {
		this(duration, false);
	}
	
	
	public RealtimeInterval(long duration, boolean fire_now) {
		super();
		this.duration = duration;
		if (fire_now) {
			this.next_check_time = System.currentTimeMillis(); // Fire straight away
		} else {
			this.next_check_time = System.currentTimeMillis() + duration;
		}
	}
	
	
	public void restartTimer() {
		this.next_check_time = System.currentTimeMillis() + duration;
	}

	
	public void setInterval(long amt, boolean restart) {
		duration = amt;
		
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

}
