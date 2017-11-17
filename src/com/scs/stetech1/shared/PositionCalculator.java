package com.scs.stetech1.shared;

import java.util.LinkedList;

import com.scs.stetech1.server.Settings;

public final class PositionCalculator {

	private LinkedList<EntityPositionData> positionData = new LinkedList<>(); // Newest entry is at the start
	private int maxEntries;
	private boolean cleardown;

	public PositionCalculator(boolean _cleardown, int _maxEntries) {
		super();

		maxEntries = _maxEntries;
		cleardown = _cleardown;
	}


	/*public PositionCalculator(int _maxEntries) {
		this(false, _maxEntries);
	}
*/

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
			this.cleardown(maxEntries);
		}
	}


	public EntityPositionData calcPosition(long serverTimeToUse) {
		synchronized (positionData) {
			if (this.positionData.size() > 0) {

				if (this.positionData.getFirst().serverTimestamp < serverTimeToUse) {
					long startDiff = serverTimeToUse - positionData.getFirst().serverTimestamp;
					//Settings.p(startDiff + " too soon");
					Settings.p(startDiff + " too soon!\n" + this.toString(serverTimeToUse));
					return this.positionData.getFirst(); // Our selected time is too soon!
				} else if (this.positionData.getLast().serverTimestamp > serverTimeToUse) {
					Settings.p(this.toString(serverTimeToUse));
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
					pos++;
				}
				throw new RuntimeException("Should not get here!");
			}
			//Settings.p("No position data (" + positionData.size() + " entries)");
		}
		return null;

	}


	private void cleardown(int num) {
		while (this.positionData.size() > num) {
			this.positionData.removeLast();
		}

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
