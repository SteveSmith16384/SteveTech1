package com.scs.stevetech1.shared;

import java.util.LinkedList;

import com.scs.stevetech1.server.Globals;

public class ChronologicalLookup<T extends ITimeStamped> {

	private LinkedList<T> positionData = new LinkedList<>(); // Newest entry is at the start
	private int maxEntries;
	private boolean cleardown;

	public ChronologicalLookup(boolean _cleardown, int _maxEntries) {
		super();

		maxEntries = _maxEntries;
		cleardown = _cleardown;
	}


	public void addData(T data) {
		synchronized (positionData) {
			boolean added = false;
			for(int i=0 ; i<this.positionData.size() ; i++) { // Goes backwards in time, number gets smaller
				T epd = this.positionData.get(i);
				if (!added) {
					if (data.getTimestamp() > epd.getTimestamp()) {
						positionData.add(i, data);
						added = true;
						break;
					}
				}
			}
			if (!added) {
				// Add to end
				positionData.add(data);
			}
			this.cleardown(maxEntries);
		}
	}


	public T calcPosition(long serverTimeToUse, boolean warn) { // todo - rename
		synchronized (positionData) {
			if (this.positionData.size() > 0) {
				if (this.positionData.getFirst().getTimestamp() < serverTimeToUse) {
					// Requested time is too soon
					if (warn) {
						//long diff = System.currentTimeMillis() - serverTimeToUse; // todo - remove
						long startDiff = serverTimeToUse - positionData.getFirst().getTimestamp();
						Globals.p("Warning: Requested time is " + startDiff + " too soon");
						//Globals.p(startDiff + " too soon!\n" + this.toString(serverTimeToUse));
					}
					return this.positionData.getFirst(); // Our selected time is too soon!
				} else if (this.positionData.getLast().getTimestamp() > serverTimeToUse) {
					if (warn) {
						//Globals.p(this.toString(serverTimeToUse));
						Globals.p("Warning: Requested time is too late");
					}
					return this.positionData.getLast(); // Our selected time is too late!
				}

				T firstEPD = null;
				int pos = 0;
				for(T secondEPD : this.positionData) { // Time gets earlier, number goes down
					if (firstEPD != null) {
						if (firstEPD.getTimestamp() >= serverTimeToUse && secondEPD.getTimestamp() <= serverTimeToUse) {
							if (cleardown) {
								this.cleardown(pos+4);
							}
							return secondEPD; // return the first one, chronologically
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
		if (num > 0) {
			while (this.positionData.size() > num) {
				this.positionData.removeLast();
			}
		}
	}


	public void clear() {
		synchronized (positionData) {
			positionData.clear();
		}
	}


}
