package ssmith.util;

import com.scs.stevetech1.server.Globals;

public class FixedLoopTime {

	private long duration;
	private long startTime = -1;

	public FixedLoopTime(long _duration) {
		this.duration = _duration;
	}


	public void start() {
		startTime = System.currentTimeMillis();
	}


	public void waitForFinish() {
		if (startTime < 0) {
			throw new RuntimeException("Start time not set");
		}
		
		long now = System.currentTimeMillis();
		long diff = duration - (now - startTime); 
		if (diff > 0) {
			try {
				Thread.sleep(diff);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		} else {
			if (Globals.SHOW_IF_SYSTEM_TOO_SLOW) {
			Globals.p("Too slow! " + diff);
			}
		}
	}

}
