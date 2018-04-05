package com.scs.stevetech1.unittests;

import org.junit.Test;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.HistoricalPositionCalculator;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.PositionCalculator;

public class TestHistoricalPositionCalculator {

	public TestHistoricalPositionCalculator() {
	}


	@Test
	private void basicPositionCalc1() {
		// Players moves at 1f per 100ms
		PositionCalculator serverPositions = new PositionCalculator(false, 1000);
		PositionCalculator clientPositions = new PositionCalculator(false, 1000);
		for (int i=0 ; i<10 ; i++) {
			serverPositions.addPositionData(new Vector3f(i, 0, 0), i*100);
			// Client is 1f ahead
			clientPositions.addPositionData(new Vector3f(i+1, 0, 0), i*100);
		}

		//Vector3f currentClientPos = new Vector3f(11, 0, 0);
		long time = 800;
		Globals.p(clientPositions.toString(time));
		Vector3f newPos = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositions, clientPositions, time);
		Vector3f correctDiff = new Vector3f(0f, 0, 0); 
		
		float diff = correctDiff.distance(newPos); 
		if (diff > 0.01f) {
			throw new RuntimeException("basicPositionCalc1 Failed: Diff is " + diff);
		}
	}

}
