package com.scs.stevetech1.unittests;

import org.junit.Test;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.HistoricalPositionCalculator;
import com.scs.stevetech1.client.syncposition.InstantPositionAdjustment;
import com.scs.stevetech1.components.IPhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.PositionCalculator;
import com.scs.stevetech1.unittests.dummyentities.DummyPhysicalEntity;

import ssmith.util.FixedLoopTime;

public class TestInstantPositionAdjustment {

	public static void main(String args[]) {
		new TestInstantPositionAdjustment().TestMoveSlowlyToCorrectPosition1();
	}


	@Test
	public void TestMoveSlowlyToCorrectPosition1() {
		IPhysicalEntity serverEntity = new DummyPhysicalEntity();
		IPhysicalEntity clientEntity = new DummyPhysicalEntity();

		PositionCalculator serverPositionData = new PositionCalculator(false, -1);
		PositionCalculator clientAvatarPositionData = new PositionCalculator(false, -1);

		FixedLoopTime loopTimer = new FixedLoopTime(Globals.SERVER_TICKRATE_MS);

		InstantPositionAdjustment ipa = new InstantPositionAdjustment();
		long start = System.currentTimeMillis();

		for (int t=0 ; t<100000 ; t+=100) {
			//loopTimer.start();

			long now = t;//System.currentTimeMillis();

			serverPositionData.addPositionData(serverEntity.getWorldTranslation(), null, now);

			clientAvatarPositionData.addPositionData(clientEntity.getWorldTranslation(), null, now);

			Vector3f adj = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, now-1000, 100);
			if (adj != null && adj.length() > 0) {
				ipa.adjustPosition(clientEntity, adj, 1);
			}

			clientEntity.adjustWorldTranslation(new Vector3f(.1f, 0, 0));

			long simpleTime = now-start;
			Globals.p("Client pos at " + simpleTime + ": " + clientEntity.getWorldTranslation());

			//loopTimer.waitForFinish();
		}
	}

}
