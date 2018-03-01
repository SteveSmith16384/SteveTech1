package ssmith.util;

import java.util.LinkedList;
import java.util.List;

public class AverageNumberCalculator {

	public List<Long> numbers = new LinkedList<>();
	private int spread;
	private long average;

	public AverageNumberCalculator(int _spread) {
		spread = _spread;
	}
	
	
	public long add(long number) {
		this.numbers.add(number);
		
		while (this.numbers.size() > spread) {
			this.numbers.remove(0);
		}
		long tot = 0;
		for (long l : this.numbers) {
			tot += l; 
		}
		average = tot / this.numbers.size();
		return average;
	}
	
	
	public long getAverage() {
		return average;
	}


}
