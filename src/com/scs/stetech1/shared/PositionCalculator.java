package com.scs.stetech1.shared;

import com.jme3.math.Vector3f;
import com.scs.stetech1.client.SorcerersClient;
import com.scs.stetech1.client.entities.PhysicalEntity;

public class PositionCalculator extends AbstractPositionCalculator { // todo - rename

	//	private List<EntityPositionData> positionData = new LinkedList<>();

	private SorcerersClient mainApp;
	private PhysicalEntity entity;
	
	public PositionCalculator(SorcerersClient _mainApp, PhysicalEntity _entity) {
		mainApp = _mainApp;
		entity = _entity;
	}

/*
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
	}*/

/*
	public void calcPosition(SorcerersClient mainApp, PhysicalEntity entity, long serverTimeToUse) {
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

						return;
					} else if (firstEPD.serverTimestamp < serverTimeToUse) {
						// Data is too old!
						Settings.p("Position data too old for " + entity.name + " (" + positionData.size() + " entries)");
						return;
					}
				}
			}
			//Settings.p("No position data for " + this.name + " (" + positionData.size() + " entries)");
		}

*/
		@Override
		public void gotData(EntityPositionData firstEPD, EntityPositionData secondEPD, long serverTimeToUse) {
			// interpolate between timestamps
			float frac = (firstEPD.serverTimestamp - serverTimeToUse) / (serverTimeToUse - secondEPD.serverTimestamp);
			Vector3f posToSet = firstEPD.position.interpolate(secondEPD.position, frac);
			if (mainApp.getPlayersAvatar() == this) {
				// if our avatar, adjust us, don't just jump to new position
				// todo - check where we should be based on where we were 100ms ago
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

		/*
	public void clearPositiondata() {
		synchronized (positionData) {
			positionData.clear();
		}
	}
		 */

	}
