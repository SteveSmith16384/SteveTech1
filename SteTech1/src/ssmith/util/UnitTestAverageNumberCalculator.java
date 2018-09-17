package ssmith.util;

import org.junit.Test;

public class UnitTestAverageNumberCalculator {

	public UnitTestAverageNumberCalculator() {
	}


	@Test
	public void basicTest1() {
		AverageNumberCalculator apt = new AverageNumberCalculator(4);
		apt.add(100);
		apt.add(150);
		long avg = apt.add(200);

		if (avg != 150) {
			throw new RuntimeException("failed");
		}

	}

}
