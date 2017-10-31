package com.scs.stetech1.shared;

import java.util.LinkedList;
import java.util.List;

public final class PositionCalculator {

	private List<EntityPositionData> positionData = new LinkedList<>();

	public PositionCalculator() {

	}


	public void addPositionData(EntityPositionData newData) {
		synchronized (positionData) {
			for(int i=0 ; i<this.positionData.size() ; i++) {
				// Time gets earlier, number gets smaller
				EntityPositionData epd = this.positionData.get(i);
				if (newData.serverTimestamp > epd.serverTimestamp) {
					positionData.add(i, newData);
					// Remove later entries
					/*todo - re-add while (this.positionData.size() > i+3) { // todo - base on time
						this.positionData.remove(i+3);
					}*/
					return;
				}
			}
			// Add to end
			positionData.add(newData);
		}
	}


	public EntityPositionData calcPosition(long serverTimeToUse) {
		synchronized (positionData) {
			if (this.positionData.size() > 1) {
				EntityPositionData firstEPD = null;
				for(EntityPositionData secondEPD : this.positionData) {
					// Time gets earlier, number goes down
					if (firstEPD == null) {
						firstEPD = secondEPD;
						if (serverTimeToUse > secondEPD.serverTimestamp) {
							return null; // Too early!
						}
					} else if (firstEPD.serverTimestamp > serverTimeToUse && secondEPD.serverTimestamp < serverTimeToUse) {
						return this.getInterpolatedPosition(firstEPD, secondEPD, serverTimeToUse);
					} else if (firstEPD.serverTimestamp < serverTimeToUse) {
						// Data is too old!
						//Settings.p("Position data too old for " + entity.name + " (" + positionData.size() + " entries)");
						return null;
					}
					firstEPD = secondEPD;
				}
			}
			//Settings.p("No position data for " + this.name + " (" + positionData.size() + " entries)");
		}
		return null;

	}


	public EntityPositionData getInterpolatedPosition(EntityPositionData firstEPD, EntityPositionData secondEPD, long serverTimeToUse) {
		// interpolate between timestamps
		return firstEPD.getInterpol(secondEPD, serverTimeToUse);
		/*float frac = (firstEPD.serverTimestamp - serverTimeToUse) / (serverTimeToUse - secondEPD.serverTimestamp);
		Vector3f posToSet = firstEPD.position.interpolate(secondEPD.position, frac);

		Quaternion newRot = new Quaternion();
		Quaternion newRot2 = newRot.slerp(firstEPD.rotation, secondEPD.rotation, frac);

		EntityPositionData epd = new EntityPositionData();
		epd.position = posToSet;
		epd.rotation = newRot2;
		epd.serverTimestamp = serverTimeToUse;
		return epd;
/*		if (mainApp.getPlayersAvatar() == this) {
			// if our avatar, adjust us, don't just jump to new position
			//todo - re-add newPos = newPos.interpolate(this.getWorldTranslation(), .5f); // Move us halfway?
			entity.scheduleNewPosition(mainApp, posToSet);
		} else
			//todo - re-add if (!newPos.equals(this.getWorldTranslation())) {
			entity.scheduleNewPosition(mainApp, posToSet);
		//}
		/*todo if (module.getPlayersAvatar() == this) {
	// if its our avatar, don't adjust rotation!
} else {
//Quaternion newRot = new Quaternion(); todo - this
//final Quaternion newRot2 = newRot.slerp(firstEPD.rotation, secondEPD.rotation, frac);
	this.scheduleNewRotation(mainApp, tempNewRot);
	//Settings.p("Updated avatar pos: " + newPos);
}*/	
	}



	public void clearPositiondata() {
		synchronized (positionData) {
			positionData.clear();
		}
	}


}
