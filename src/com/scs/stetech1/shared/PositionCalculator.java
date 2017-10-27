package com.scs.stetech1.shared;

import java.util.LinkedList;
import java.util.List;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stetech1.client.SorcerersClient;

public class PositionCalculator {

	private List<EntityPositionData> positionData = new LinkedList<>();

	public PositionCalculator() {

	}


	public void addPositionData(EntityPositionData newData) {
		synchronized (positionData) {
			for(int i=0 ; i<this.positionData.size() ; i++) {
				// Time gets earlier
				EntityPositionData epd = this.positionData.get(i);
				if (epd.serverTimestamp < newData.serverTimestamp) {
					positionData.add(i, newData);
					// Remove later entries
					while (this.positionData.size() > i+3) {
						this.positionData.remove(i+1);
					}
					return;
				}
			}
			// Add to end
			positionData.add(newData);
		}
	}


	public void calcPosition(SorcerersClient mainApp, long serverTimeToUse, Vector3f posToSet, Quaternion rotToSet) {
		synchronized (positionData) {
			if (this.positionData.size() > 1) {
				EntityPositionData firstEPD = null;
				for(EntityPositionData secondEPD : this.positionData) {
					// Time gets earlier
					if (firstEPD == null) {
						firstEPD = secondEPD;
						if (secondEPD.serverTimestamp < serverTimeToUse) {
							return; // Too early!
						}
					} else if (firstEPD.serverTimestamp > serverTimeToUse && secondEPD.serverTimestamp < serverTimeToUse) {
						// interpolate between timestamps
						float frac = (firstEPD.serverTimestamp - serverTimeToUse) / (serverTimeToUse - secondEPD.serverTimestamp);
						posToSet.set(firstEPD.position.interpolate(secondEPD.position, frac));
						rotToSet.slerp(firstEPD.rotation, secondEPD.rotation, frac);
						return;
					}
				}
				// If we got this far, all position data is too old!
				//Settings.p("Position data too old for " + this.name + " (" + positionData.size() + " entries)");
			}
			//Settings.p("No position data for " + this.name + " (" + positionData.size() + " entries)");
		}

	}


	public void clearPositiondata() {
		synchronized (positionData) {
			positionData.clear();
		}
	}


}
