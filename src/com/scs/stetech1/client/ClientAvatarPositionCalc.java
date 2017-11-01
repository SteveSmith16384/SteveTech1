package com.scs.stetech1.client;

import com.jme3.math.Vector3f;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.PositionCalculator;

public class ClientAvatarPositionCalc {

	public ClientAvatarPositionCalc() {

	}


	/**
	 * This calculates the difference between what the client and server think the position should be
	 */
	public static Vector3f calcHistoricalPositionOffset(PositionCalculator serverPositionData, PositionCalculator clientAvatarPositionData, long serverTimeToUse, long ping) {
		EntityPositionData serverEPD = serverPositionData.calcPosition(serverTimeToUse);
		if (serverEPD != null) {
			long clientTimeToUse = serverTimeToUse - ping;
			// check where we should be based on where we were X ms ago
			EntityPositionData clientEPD = clientAvatarPositionData.calcPosition(clientTimeToUse);
			if (clientEPD != null) {
				// Is there a difference
				float diff = serverEPD.position.distance(clientEPD.position);
				if (diff > 0.1) {
					// There should be no difference!
					Settings.p("Server " + serverPositionData.toString(serverTimeToUse));
					Settings.p("Client " + clientAvatarPositionData.toString(clientTimeToUse));
				}
				return clientEPD.position.subtract(serverEPD.position);
			}
		}
		return null;

	}


	public static Vector3f calcHistoricalPosition_UNUSED(Vector3f currentClientAvatarPosition_UNUSED, PositionCalculator serverPositionData, PositionCalculator clientAvatarPositionData, long serverTimeToUse, long ping) {
		EntityPositionData serverEPD = serverPositionData.calcPosition(serverTimeToUse);
		if (serverEPD != null) {
			long clientTimeToUse = serverTimeToUse - ping;
			// check where we should be based on where we were X ms ago
			EntityPositionData clientEPD = clientAvatarPositionData.calcPosition(clientTimeToUse);
			if (clientEPD != null) {
				// Is there a difference
				float diff = serverEPD.position.distance(clientEPD.position);
				if (diff > 0.1) {
					// There should be no difference!
					Settings.p("Server " + serverPositionData.toString(serverTimeToUse));
					Settings.p("Client " + clientAvatarPositionData.toString(clientTimeToUse));
				}
				//Settings.p("Adjusting client: " + diff);

				// OPTION 1: Get diff between player pos X millis ago and current pos, and re-add this to server pos
				/*Vector3f clientMovementSinceRenderDelay = currentClientAvatarPosition.subtract(clientEPD.position);
					//clientMovementSinceRenderDelay.y = 0; // Don't adjust y-axis
					Vector3f newPos = serverEPD.position.add(clientMovementSinceRenderDelay);*/

				// OPTION 2: Adjust player by halfway between server pos and client pos
				/*Vector3f newPos = new Vector3f();
					newPos.interpolate(serverEPD.position, clientEPD.position, .5f); // todo - just move to 
					Settings.p("Moving player to " + newPos);*/

				// OPTION 3: Move player slowly towards server position
				Vector3f offset = serverEPD.position.subtract(clientEPD.position).normalizeLocal();
				offset.multLocal(0.1f);
				Vector3f newPos = clientEPD.position.add(offset);

				if (newPos.y < 0.4f || newPos.y > 7) {
					Settings.p("Too far!");
				}
				return newPos; // Always return something
			}
		}
		return null;

	}

}
