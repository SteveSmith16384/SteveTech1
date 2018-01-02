package com.scs.stevetech1.client;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.PositionCalculator;

public class HistoricalPositionCalculator {

	public HistoricalPositionCalculator() {

	}


	/**
	 * This calculates the difference between what the client and server think the position should be,
	 * so this can be added to the clients current position to get the correct position
	 */
	public static Vector3f calcHistoricalPositionOffset(PositionCalculator serverPositionData, PositionCalculator clientAvatarPositionData, long serverTimeToUse, long ping) {
		EntityPositionData serverEPD = serverPositionData.calcPosition(serverTimeToUse);
		if (serverEPD != null) {
			long clientTimeToUse = serverTimeToUse - ping;
			// check where we should be based on where we were X ms ago
			EntityPositionData clientEPD = clientAvatarPositionData.calcPosition(clientTimeToUse);
			if (clientEPD != null) {
				// Is there a difference
				/*float diff = serverEPD.position.distance(clientEPD.position);
				if (diff > 0.1) {
					// There should be no difference!
					Settings.p("Server " + serverPositionData.toString(serverTimeToUse));
					Settings.p("Client " + clientAvatarPositionData.toString(clientTimeToUse));
				}*/
				return serverEPD.position.subtract(clientEPD.position);
			}
		}
		return null;

	}


}
