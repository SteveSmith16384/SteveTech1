package com.scs.stevetech1.entities;

import com.scs.stevetech1.client.AbstractGameClient;

public abstract class NonAdjustedEntity extends PhysicalEntity {

	@Override
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse, float tpf_secs) {
		if (this.shooter == mainApp.currentAvatar) {
			// Do nothing
		} else {
			//super.calcPosition(mainApp, serverTimeToUse, tpf_secs);
		}
		/*if (launched) {
			if (Globals.SYNC_GRENADE_POS) {
				if (this.serverPositionData.hasRecentData(serverTimeToUse)) { // todo - remove
					Vector3f offset = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientSidePositionData, serverTimeToUse);
					if (offset != null) {
						this.syncPos.adjustPosition(this, offset, tpf_secs);
					}
				}
			}
		}*/
	}



}
