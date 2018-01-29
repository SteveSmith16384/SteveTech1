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
	 * so this can be added to the clients current position to get the correct position.
	 */
	public static Vector3f calcHistoricalPositionOffset(PositionCalculator serverPositionData, PositionCalculator clientAvatarPositionData, long serverTimeToUse) {
		if (serverPositionData.hasRecentData(serverTimeToUse)) {
			//if (clientAvatarPositionData.hasRecentData(serverTimeToUse)) {
			EntityPositionData serverEPD = serverPositionData.calcPosition(serverTimeToUse, true);
			if (serverEPD != null) {
				long clientTimeToUse = serverTimeToUse;// - ping;
				// check where we should be based on where we were X ms ago
				EntityPositionData clientEPD = clientAvatarPositionData.calcPosition(clientTimeToUse, true);
				if (clientEPD != null) {
					// Is there a difference
					/*float diff = serverEPD.position.distance(clientEPD.position);
					if (diff > 0.2) {
						// There should be no difference!
						//Globals.p("Server " + serverPositionData.toString(serverTimeToUse));
						//Globals.p("Client " + clientAvatarPositionData.toString(clientTimeToUse));
					}*/
					Vector3f vdiff = serverEPD.position.subtract(clientEPD.position); 
					return vdiff;
				}
			//}
			}
		}
		return null;

	}


}
