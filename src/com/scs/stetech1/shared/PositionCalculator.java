package com.scs.stetech1.shared;

import java.util.LinkedList;

import com.scs.stetech1.server.Settings;

public final class PositionCalculator {

	private LinkedList<EntityPositionData> positionData = new LinkedList<>(); // Newest entry is at the start
	private int maxEntries;

	public PositionCalculator(int _maxEntries) {
		super();

		maxEntries = _maxEntries;
	}


	public void addPositionData(EntityPositionData newData) {
		synchronized (positionData) {
			boolean added = false;
			for(int i=0 ; i<this.positionData.size() ; i++) { // Goes backwards in time, number gets smaller
				EntityPositionData epd = this.positionData.get(i);
				if (!added) {
					if (newData.serverTimestamp > epd.serverTimestamp) {
						positionData.add(i, newData);
						added = true;
						break;
					}
				}
			}
			if (!added) {
				// Add to end
				positionData.add(newData);
			}

			// Remove later entries
			//int min_entries = 5000 / Settings.SERVER_SEND_UPDATE_INTERVAL_MS;
			//long cutoff = System.currentTimeMillis() - (Settings.SERVER_SEND_UPDATE_INTERVAL_MS*2);
			while (this.positionData.size() > maxEntries) {
				EntityPositionData epd = this.positionData.getLast();
				this.positionData.removeLast();
			}
		}
	}


	public EntityPositionData calcPosition(long serverTimeToUse) {
		synchronized (positionData) {
			if (this.positionData.size() > 1) {

				if (this.positionData.getFirst().serverTimestamp < serverTimeToUse) {
					//Settings.p(this.toString(serverTimeToUse));
					//long startDiff = serverTimeToUse - positionData.getFirst().serverTimestamp;
					//Settings.p(startDiff + " too soon");
					return null; // Our selected time is too soon!
				} else if (this.positionData.getLast().serverTimestamp > serverTimeToUse) {
					//Settings.p(this.toString(serverTimeToUse));
					return null; // Our selected time is too late!
				}

				EntityPositionData firstEPD = null;
				for(EntityPositionData secondEPD : this.positionData) { // Time gets earlier, number goes down
					if (firstEPD != null) {
						if (firstEPD.serverTimestamp >= serverTimeToUse && secondEPD.serverTimestamp <= serverTimeToUse) {
							return this.getInterpolatedPosition(firstEPD, secondEPD, serverTimeToUse); //positionData.indexOf(firstEPD); 
						}/* else if (firstEPD.serverTimestamp < serverTimeToUse) {
							// Data is too old!
							Settings.p(this.toString(serverTimeToUse));
							//long diff = serverTimeToUse - firstEPD.serverTimestamp;
							//Settings.p("Position data too old for " + serverTimeToUse + " by " + diff + " (" + positionData.size() + " entries)");
							//Settings.p("Data goes from " + positionData.getFirst().serverTimestamp + " to " + positionData.getLast().serverTimestamp);
							return null;
						}*/
					}
					firstEPD = secondEPD;
				}
				throw new RuntimeException("Should not get here!");
			}
			Settings.p("No position data (" + positionData.size() + " entries)");
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


	public String toString(long showTime) {
		StringBuilder str = new StringBuilder();
		str.append("OUTPUT:-\n");
		boolean shownTime = false;
		for(int i=0 ; i<this.positionData.size() ; i++) { // Time gets earlier, number gets smaller
			EntityPositionData epd = this.positionData.get(i);
			if (!shownTime && showTime > epd.serverTimestamp) {
				str.append("Here: " + showTime + "\n");
				shownTime = true;
			}
			str.append(i + ": " + epd.serverTimestamp + " - " + epd.position + "\n");
		}		

		return str.toString();
	}
}
