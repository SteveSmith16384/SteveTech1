package ssmith.util;

public class FixedLoopTime {

	private long duration;
	private long startTime = -1;

	public FixedLoopTime(long _duration) {
		this.duration = _duration;
	}


	public void start() {
		startTime = System.nanoTime();
	}


	public void waitForFinish() {
		if (startTime < 0) {
			throw new RuntimeException("todo");
		}
		long now = System.nanoTime();
		if (now - startTime < duration) {
			try {
				Thread.sleep(duration - (now - startTime));
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}

}
