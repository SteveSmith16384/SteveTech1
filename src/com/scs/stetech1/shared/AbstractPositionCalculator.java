package com.scs.stetech1.shared;

import java.util.LinkedList;
import java.util.List;

import com.jme3.math.Vector3f;
import com.scs.stetech1.client.SorcerersClient;
import com.scs.stetech1.client.entities.PhysicalEntity;
import com.scs.stetech1.server.Settings;

public abstract class AbstractPositionCalculator {

	private List<EntityPositionData> positionData = new LinkedList<>();
	// private lop

	public AbstractPositionCalculator() {
		//maxData = _maxData;
	}


	public void addPositionData(EntityPositionData newData) {
		synchronized (positionData) {
			for(int i=0 ; i<this.positionData.size() ; i++) {
				// Time gets earlier
				EntityPositionData epd = this.positionData.get(i);
				if (epd.serverTimestamp < newData.serverTimestamp) {
					positionData.add(i, newData);
					// Remove later entries
					while (this.positionData.size() > i+3) { // todo - base on time
						this.positionData.remove(i+1);
					}
					return;
				}
			}
			// Add to end
			positionData.add(newData);
		}
	}


	public void calcPosition(long serverTimeToUse) {
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
						this.gotData(firstEPD, secondEPD, serverTimeToUse);
						return;
					} else if (firstEPD.serverTimestamp < serverTimeToUse) {
						// Data is too old!
						//Settings.p("Position data too old for " + entity.name + " (" + positionData.size() + " entries)");
						return;
					}
				}
			}
			//Settings.p("No position data for " + this.name + " (" + positionData.size() + " entries)");
		}

	}


	public abstract void gotData(EntityPositionData firstEPD, EntityPositionData secondEPD, long serverTimeToUse);


	public void clearPositiondata() {
		synchronized (positionData) {
			positionData.clear();
		}
	}


}
