package com.scs.stetech1.unittests;

import com.jme3.math.Vector3f;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.PositionCalculator;

public class TestAvatarPositionCalcs {

	public TestAvatarPositionCalcs() {
	}

	
	public void runTests() {
		basicPositionCalc1();
	}
	
	
	private void basicPositionCalc1() {
		PositionCalculator serverPositions = new PositionCalculator();
		for (int i=0 ; i<10 ; i++) {
			serverPositions.addPositionData(new EntityPositionData(new Vector3f(i, 0, 0), null, i*100));
		}

		PositionCalculator clientPositions = new PositionCalculator();
		// Client positions in fractions of 0.1f, and client if 1f in front
		for (int i=0 ; i<100 ; i++) {
			clientPositions.addPositionData(new EntityPositionData(new Vector3f((i/10)+1, 0, 0), null, (i/10)*100));
		}
		
		
		EntityPositionData after150 = serverPositions.calcPosition(150);
		Vector3f correctPos = new Vector3f(1.5f, 0, 0);
		float diff = after150.position.distance(correctPos); 
		if (diff != 0) {
			Settings.p("basicPositionCalc1 Failed: Diff is " + diff);
		}
	}



}
