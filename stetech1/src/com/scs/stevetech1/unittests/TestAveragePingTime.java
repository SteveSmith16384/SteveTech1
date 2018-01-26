package com.scs.stevetech1.unittests;

import org.junit.Test;

import com.scs.stevetech1.shared.AverageNumberCalculator;

public class TestAveragePingTime {

	public TestAveragePingTime() {
	}


	@Test
	private void basicTest1() {
		AverageNumberCalculator apt = new AverageNumberCalculator();
		apt.add(100);
		apt.add(150);
		long avg = apt.add(200);

		if (avg != 150) {
			throw new RuntimeException("failed");
		}

	}

}
