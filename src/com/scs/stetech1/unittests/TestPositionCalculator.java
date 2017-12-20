package com.scs.stetech1.unittests;

import com.jme3.math.Vector3f;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.PositionCalculator;

public class TestPositionCalculator {

	public TestPositionCalculator() {
	}


	public void runTests() {
		basicPositionCalc1();
		basicPositionCalc2_AddInReverse();
		basicPositionCalc2_EarlyInSegment();
		basicPositionCalc2_LateInSegment();
		basicPositionCalc_TooEarly();
		basicPositionCalc_TooLate();
	}


	private void basicPositionCalc1() {
		PositionCalculator posCalc = new PositionCalculator(false, 1000);
		for (int i=0 ; i<10 ; i++) {
			posCalc.addPositionData(new Vector3f(i, 0, 0), null, i*100);
		}
		EntityPositionData after150 = posCalc.calcPosition(150);
		Vector3f correctPos = new Vector3f(1.5f, 0, 0);
		float diff = after150.position.distance(correctPos); 
		if (diff != 0) {
			throw new RuntimeException("basicPositionCalc1 Failed: Diff is " + diff);
		}
	}


	private void basicPositionCalc2_AddInReverse() {
		PositionCalculator posCalc = new PositionCalculator(false, 1000);
		for (int i=10 ; i>0 ; i--) {
			posCalc.addPositionData(new Vector3f(i, 0, 0), null, i*100);
		}
		EntityPositionData after350 = posCalc.calcPosition(350);
		Vector3f correctPos = new Vector3f(3.5f, 0, 0);
		float diff = after350.position.distance(correctPos); 
		if (diff != 0) {
			throw new RuntimeException("basicPositionCalc2_AddInReverse Failed: Diff is " + diff);
		}
	}


	private void basicPositionCalc2_EarlyInSegment() {
		PositionCalculator posCalc = new PositionCalculator(false, 1000);
		for (int i=10 ; i>0 ; i--) {
			posCalc.addPositionData(new Vector3f(i, 0, 0), null, i*100);
		}
		EntityPositionData after410 = posCalc.calcPosition(410);
		Vector3f correctPos = new Vector3f(4.1f, 0, 0);
		float diff = after410.position.distance(correctPos); 
		if (diff != 0) {
			throw new RuntimeException("basicPositionCalc2_EarlyInSegment Failed: Diff is " + diff);
		}
	}
	

	private void basicPositionCalc2_LateInSegment() {
		PositionCalculator posCalc = new PositionCalculator(false, 1000);
		for (int i=10 ; i>0 ; i--) {
			posCalc.addPositionData(new Vector3f(i, 0, 0), null, i*100);
		}
		EntityPositionData after490 = posCalc.calcPosition(490);
		Vector3f correctPos = new Vector3f(4.9f, 0, 0);
		float diff = after490.position.distance(correctPos); 
		if (diff != 0) {
			throw new RuntimeException("basicPositionCalc2_LateInSegment Failed: Diff is " + diff);
		}
	}


	private void basicPositionCalc_TooEarly() {
		PositionCalculator posCalc = new PositionCalculator(false, 1000);
		for (int i=10 ; i>0 ; i--) {
			posCalc.addPositionData(new Vector3f(i, 0, 0), null, 100+(i*100));
		}
		EntityPositionData after2000 = posCalc.calcPosition(2000);
		if (after2000 != null) {
			throw new RuntimeException("basicPositionCalc_TooEarly Failed: Should be null");
		}
	}


	private void basicPositionCalc_TooLate() {
		PositionCalculator posCalc = new PositionCalculator(false, 1000);
		for (int i=10 ; i>0 ; i--) {
			posCalc.addPositionData(new Vector3f(i, 0, 0), null, 100+(i*100));
		}
		EntityPositionData after0 = posCalc.calcPosition(0);
		if (after0 != null) {
			throw new RuntimeException("basicPositionCalc_TooEarly Failed: Should be null");
		}
	}


}
