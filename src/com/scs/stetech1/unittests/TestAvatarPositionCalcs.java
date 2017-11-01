package com.scs.stetech1.unittests;

import com.jme3.math.Vector3f;
import com.scs.stetech1.client.ClientAvatarPositionCalc;
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
		int ping = 100;
		// Players moves at 1f per 100ms
		PositionCalculator serverPositions = new PositionCalculator(1000);
		PositionCalculator clientPositions = new PositionCalculator(1000);
		for (int i=0 ; i<10 ; i++) {
			serverPositions.addPositionData(new EntityPositionData(new Vector3f(i, 0, 0), null, i*100));
			// Client is 1f ahead
			clientPositions.addPositionData(new EntityPositionData(new Vector3f(i+1, 0, 0), null, i*100));
		}

		Vector3f currentClientPos = new Vector3f(11, 0, 0);
		long time = 800;
		Settings.p(clientPositions.toString(time));
		Vector3f newPos = ClientAvatarPositionCalc.calcHistoricalPosition(currentClientPos, serverPositions, clientPositions, time, ping);
		Vector3f correctPos = new Vector3f(8f, 0, 0); 
		
		float diff = correctPos.distance(newPos); 
		if (diff > 0.01f) {
			throw new RuntimeException("basicPositionCalc1 Failed: Diff is " + diff);
		}
	}



}
