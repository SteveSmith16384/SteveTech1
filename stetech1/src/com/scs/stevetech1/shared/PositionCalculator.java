package com.scs.stevetech1.shared;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.server.Globals;

public final class PositionCalculator {

	private LinkedList<EntityPositionData> positionData = new LinkedList<>(); // Newest entry is at the start
	private LinkedList<EntityPositionData> oldPositionData = new LinkedList<>();
	private int maxEntries;
	private boolean cleardown;

	public PositionCalculator(boolean _cleardown, int _maxEntries) {
		super();

		maxEntries = _maxEntries;
		cleardown = _cleardown;
	}


	public EntityPositionData getMostRecent() {
		return positionData.getFirst();
	}


	public EntityPositionData getOldest() {
		return positionData.getLast();
	}


	public void addPositionData(Vector3f pos, Quaternion q, long time) {
		//long diff = System.currentTimeMillis() - time;

		EntityPositionData newData = null;
		if (this.oldPositionData.size() > 0) {
			newData = this.oldPositionData.removeLast();
		} else {
			newData = new EntityPositionData();
		}
		newData.position.set(pos);
		if (q != null) {
			newData.rotation.set(q);
		}
		newData.serverTimestamp = time;

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
			this.cleardown(maxEntries);
		}
	}


	public boolean hasRecentData(long serverTimeToUse) {
		try {
			EntityPositionData epd = this.positionData.getFirst(); 
			if (epd != null) {
				long diff = epd.serverTimestamp - serverTimeToUse;
				return diff >= 0;
			}
		} catch (NoSuchElementException ex) {
			// todo - why do we get this?
		}
		return false;
	}

	public EntityPositionData calcPosition(long serverTimeToUse, boolean warn) {
		synchronized (positionData) {
			if (this.positionData.size() > 0) {
				if (this.positionData.getFirst().serverTimestamp < serverTimeToUse) {
					// Requested time is too soon
					if (warn) {
						//long diff = System.currentTimeMillis() - serverTimeToUse;
						long startDiff = serverTimeToUse - positionData.getFirst().serverTimestamp;
						Globals.p("Warning: Requested time is " + startDiff + " too soon");
						Globals.p(startDiff + " too soon!\n" + this.toString(serverTimeToUse));
					}
					return this.positionData.getFirst(); // Our selected time is too soon!
				} else if (this.positionData.getLast().serverTimestamp > serverTimeToUse) {
					if (warn) {
						//Globals.p(this.toString(serverTimeToUse));
						long diff = this.positionData.getLast().serverTimestamp - serverTimeToUse;
						Globals.p("Warning: Requested time is too late by " + diff);
					}
					return this.positionData.getLast(); // Our selected time is too late!
				}

				EntityPositionData firstEPD = null;
				int pos = 0;
				for(EntityPositionData secondEPD : this.positionData) { // Time gets earlier, number goes down
					if (firstEPD != null) {
						if (firstEPD.serverTimestamp >= serverTimeToUse && secondEPD.serverTimestamp <= serverTimeToUse) {
							if (cleardown) {
								this.cleardown(pos+4);
							}
							//return this.getInterpolatedPosition(firstEPD, secondEPD, serverTimeToUse); //positionData.indexOf(firstEPD);
							return firstEPD.getInterpol(secondEPD, serverTimeToUse);
						}
					}
					firstEPD = secondEPD;
					pos++;
				}
				//throw new RuntimeException("Should not get here!");
			}
		}
		return null;

	}


	private void cleardown(int num) {
		if (num >= 0) {
			while (this.positionData.size() > num) {
				EntityPositionData epd = this.positionData.removeLast();
				this.oldPositionData.add(epd);
			}
		}
	}


	public void clear() {
		this.oldPositionData.addAll(this.positionData);
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
