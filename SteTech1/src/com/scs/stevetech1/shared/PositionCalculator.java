package com.scs.stevetech1.shared;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.server.Globals;

public final class PositionCalculator {

	private LinkedList<EntityPositionData> positionData = new LinkedList<>(); // Newest entry is at the start
	private LinkedList<EntityPositionData> oldPositionData = new LinkedList<>(); // Keep a cache of objects to re-use

	private long historyLengthMillis;
	private String entityName;

	public PositionCalculator(long _historyLengthMillis, String _entityName) {
		super();

		historyLengthMillis = _historyLengthMillis;
		entityName = _entityName;
	}


	public EntityPositionData getMostRecent() {
		return positionData.getFirst();
	}


	public EntityPositionData getOldest() {
		return positionData.getLast();
	}


	public void addPositionData(Vector3f pos, long time) {
		EntityPositionData newData = null;
		if (this.oldPositionData.size() > 0) {
			newData = this.oldPositionData.removeLast(); // Re-use object from cache
		} else {
			newData = new EntityPositionData();
		}
		
		newData.position.set(pos);
		newData.serverTimestamp = time;

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
		this.cleardown(time);
	}


	public boolean hasRecentData(long serverTimeToUse) {
		try {
			EntityPositionData epd = this.positionData.getFirst(); 
			if (epd != null) {
				long diff = epd.serverTimestamp - serverTimeToUse;
				return diff >= 0;
			}
		} catch (NoSuchElementException ex) {
			// No data!
		}
		return false;
	}


	public boolean hasAnyData() {
		return this.positionData.size() > 0;
	}


	public EntityPositionData calcPosition(long serverTimeToUse, boolean warn) {
		synchronized (positionData) {
			if (this.positionData.size() > 0) {
				if (this.positionData.getFirst().serverTimestamp < serverTimeToUse) {
					// Requested time is too recent
					if (warn) {
						//long diff = System.currentTimeMillis() - serverTimeToUse;
						long startDiff = serverTimeToUse - positionData.getFirst().serverTimestamp;
						Globals.p("Warning: Requested time is " + startDiff + " too soon for " + this.entityName);
						//Globals.p("History data starts " + startDiff + " after requested time for " + this.entityName);// too recent for ..!\n" + this.toString(serverTimeToUse));
					}
					return this.positionData.getFirst(); // Our selected time is too soon, so return soonest we have
				} else if (this.positionData.getLast().serverTimestamp > serverTimeToUse) {
					if (warn) {
						//Globals.p(this.toString(serverTimeToUse));
						long diff = this.positionData.getLast().serverTimestamp - serverTimeToUse;
						Globals.p("Warning: History data starts " + diff + " after requested time for " + this.entityName);
					}
					return this.positionData.getLast(); // Our selected time is too late!
				}

				EntityPositionData firstEPD = null;
				for(EntityPositionData secondEPD : this.positionData) { // Time gets earlier, number goes down
					if (firstEPD != null) {
						if (firstEPD.serverTimestamp >= serverTimeToUse && secondEPD.serverTimestamp <= serverTimeToUse) {
							return firstEPD.getInterpol(secondEPD, serverTimeToUse);
						}
					}
					firstEPD = secondEPD;
				}
				//throw new RuntimeException("Should not get here!");
			}
		}
		return null;

	}


	private void cleardown(long timeOfEntryAdded) {
		long thresh = timeOfEntryAdded - this.historyLengthMillis;
		while (this.positionData.size() > 100) { // Keep at least X amount
			EntityPositionData epd = this.positionData.getLast();
			if (epd.serverTimestamp < thresh) {
				this.positionData.removeLast();
				this.oldPositionData.add(epd);
			} else {
				break;			
			}
		}
	}


	public void clear() {
		this.oldPositionData.addAll(this.positionData);
		positionData.clear();
	}


	public String toString(long showTime) {
		StringBuilder str = new StringBuilder();
		str.append("OUTPUT:-\n");
		boolean shownTime = false;
		for(int i=0 ; i<this.positionData.size() ; i++) { // Time gets earlier, number gets smaller
			EntityPositionData epd = this.positionData.get(i);
			if (!shownTime && showTime > 0 && showTime > epd.serverTimestamp) {
				str.append("Here: " + showTime + "\n");
				shownTime = true;
			}
			str.append(i + ": " + epd.serverTimestamp + " - " + epd.position + "\n");
		}		

		return str.toString();
	}

	/*
	public String toString(PositionCalculator other, long showTime) {
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
	 */
}
