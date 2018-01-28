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

		InstantPositionAdjustment ipa = new InstantPositionAdjustment();

		for (int t=0 ; t<10000 ; t+=100) {
			if (t == 2000) {
				int dfg = 456;
			}
			
			// Move server
			serverEntity.adjustWorldTranslation(new Vector3f(1f, 0, 0));
			serverPositionData.addPositionData(serverEntity.getWorldTranslation(), null, t);

			clientEntity.adjustWorldTranslation(new Vector3f(2f, 0, 0));
			clientAvatarPositionData.addPositionData(clientEntity.getWorldTranslation(), null, t);

			// Adjust client
			Vector3f adj = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, t-1000, 100);
			if (adj != null && adj.length() > 0) {
				adj = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, t-1000, 100);
				if (ipa.adjustPosition(clientEntity, adj, 1)) {
					Globals.p("Adjusting client by " + adj);

					int dfg = 456;
				}
			}

			Globals.p("Client pos at " + t + ": " + clientEntity.getWorldTranslation());
			Globals.p("Server pos at " + t + ": " + serverEntity.getWorldTranslation());

		}
		
	}

}
