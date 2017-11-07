package com.scs.stetech1.unittests;

import org.junit.Test;

import com.jme3.math.Vector3f;
import com.scs.stetech1.client.syncposition.IPositionAdjuster;
import com.scs.stetech1.client.syncposition.MoveSlowlyToCorrectPosition;
import com.scs.stetech1.server.Settings;

public class TestPositionSync {

	public TestPositionSync() {
	}


	@Test
	public void TestMoveSlowlyToCorrectPosition() {
		IPositionAdjuster posSync = new MoveSlowlyToCorrectPosition(.1f);
		
		Vector3f serverPos = new Vector3f(10f, 0.5f, 10f);
		Vector3f clientPos = new Vector3f(5f, 0.5f, 5f);
		int count = 0;
		while (serverPos.distance(clientPos) > 0.01f) {
			Vector3f adj = posSync.getNewAdjustment(serverPos.subtract(clientPos));
			clientPos.addLocal(adj);
			Settings.p("Client pos: " + clientPos);
			count++;
		}
		Settings.p("Finished: " + count);
	}
}
