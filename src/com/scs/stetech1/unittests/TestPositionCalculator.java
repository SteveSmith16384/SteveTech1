package com.scs.stetech1.unittests;

import com.jme3.math.Vector3f;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.PositionCalculator;

public class TestPositionCalculator implements IUnitTestClass {

	public TestPositionCalculator() {
	}

	@Override
	public void runTests() {
		basicPositionCalc1();



	}


	private void basicPositionCalc1() {
		PositionCalculator posCalc = new PositionCalculator();
		for (int i=0 ; i<10 ; i++) {
			posCalc.addPositionData(new EntityPositionData(new Vector3f(i, 0, 0), null, i*100));
		}
		EntityPositionData after150 = posCalc.calcPosition(150);
		Vector3f correctPos = new Vector3f(1.5f, 0, 0);
		float diff = after150.position.distance(correctPos); 
		if (diff != 0) {
			Settings.p("Failed: Diff is " + diff);
		}
	}

}
