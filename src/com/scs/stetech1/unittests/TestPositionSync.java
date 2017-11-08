package com.scs.stetech1.unittests;

import org.junit.Test;

import com.jme3.math.Vector3f;
import com.scs.stetech1.client.syncposition.ICorrectClientEntityPosition;
import com.scs.stetech1.client.syncposition.MoveSlowlyToCorrectPosition;
import com.scs.stetech1.components.IPhysicalEntity;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.unittests.dummyentities.DummyPhysicalEntity;

public class TestPositionSync {

	public TestPositionSync() {
	}


	@Test
	public void TestMoveSlowlyToCorrectPosition() {
		IPhysicalEntity pe = new DummyPhysicalEntity();
		pe.setWorldTranslation(new Vector3f(5f, 0.5f, 5f));
		
		ICorrectClientEntityPosition posSync = new MoveSlowlyToCorrectPosition(.1f);
		
		Vector3f serverPos = new Vector3f(10f, 0.5f, 10f);
		int count = 0;
		while (serverPos.distance(pe.getWorldTranslation()) > 0.01f) {
			Vector3f offset = serverPos.subtract(pe.getWorldTranslation());
			posSync.adjustPosition(pe, offset);
			Settings.p("Client pos: " + pe.getWorldTranslation());
			count++;
		}
		Settings.p("Finished: " + count);
	}
}
