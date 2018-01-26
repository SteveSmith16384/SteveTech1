package com.scs.stevetech1.unittests;

import org.junit.Test;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.syncposition.ICorrectClientEntityPosition;
import com.scs.stevetech1.client.syncposition.MoveSlowlyToCorrectPosition;
import com.scs.stevetech1.components.IPhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.unittests.dummyentities.DummyPhysicalEntity;

public class TestMoveSlowlyToCorrectPosition {

	public TestMoveSlowlyToCorrectPosition() {
	}


	@Test
	public void TestMoveSlowlyToCorrectPosition1() {
		IPhysicalEntity pe = new DummyPhysicalEntity();
		pe.setWorldTranslation(new Vector3f(5f, 0.5f, 5f));
		
		ICorrectClientEntityPosition posSync = new MoveSlowlyToCorrectPosition();
		
		Vector3f serverPos = new Vector3f(10f, 0.5f, 10f);
		int count = 0;
		while (serverPos.distance(pe.getWorldTranslation()) > 0.01f) {
			Vector3f offset = serverPos.subtract(pe.getWorldTranslation());
			posSync.adjustPosition(pe, offset, 1);
			Globals.p("Client pos: " + pe.getWorldTranslation());
			count++;
		}
		Globals.p("Finished: " + count);
	}
}
