package com.scs.stevetech1.shared;

import java.util.LinkedList;
import java.util.List;

public class AverageNumberCalculator {

	public List<Long> pingTimes = new LinkedList<>();

	public AverageNumberCalculator() {
	}
	
	
	public long add(long ping) {
		this.pingTimes.add(ping);
		
		while (this.pingTimes.size() > 10) {
			this.pingTimes.remove(0);
		}
		long tot = 0;
		for (long l : this.pingTimes) {
			tot += l; 
		}
		return tot / this.pingTimes.size();
	}


}
