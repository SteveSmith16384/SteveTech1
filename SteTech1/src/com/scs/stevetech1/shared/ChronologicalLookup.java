package com.scs.stevetech1.shared;

import java.util.LinkedList;

public class ChronologicalLookup<T extends ITimeStamped> {

	private LinkedList<T> chronoData = new LinkedList<>(); // Newest entry is at the start

	private long historyLength;

	public ChronologicalLookup(long _historyLength) {//boolean _cleardown, int _maxEntries) {
		super();

		//maxEntries = _maxEntries;
		//cleardown = _cleardown;
		historyLength = _historyLength;
	}


	public void addData(T data) {
		//synchronized (chronoData) {
		boolean added = false;
		for(int i=0 ; i<this.chronoData.size() ; i++) { // Goes backwards in time, number gets smaller
			T epd = this.chronoData.get(i);
			if (!added) {
				if (data.getTimestamp() > epd.getTimestamp()) {
					chronoData.add(i, data);
					added = true;
					break;
				}
			}
		}
		if (!added) {
			// Add to end
			chronoData.add(data);
		}
		this.cleardown(data.getTimestamp());
		//}
	}


	public T get(long serverTimeToUse, boolean warn) {
		//synchronized (chronoData) {
		if (this.chronoData.size() > 0) {
			if (this.chronoData.getFirst().getTimestamp() < serverTimeToUse) {
				// Requested time is too soon
				if (warn) {
					//long diff = System.currentTimeMillis() - serverTimeToUse;
					//long startDiff = serverTimeToUse - chronoData.getFirst().getTimestamp();
					//Globals.p("Warning: Requested time is " + startDiff + " too soon");
					//Globals.p(startDiff + " too soon!\n" + this.toString(serverTimeToUse));
				}
				return this.chronoData.getFirst(); // Our selected time is too soon!
			} else if (this.chronoData.getLast().getTimestamp() > serverTimeToUse) {
				if (warn) {
					//Globals.p(this.toString(serverTimeToUse));
					//Globals.p("Warning: Requested time is too late");
				}
				return this.chronoData.getLast(); // Our selected time is too late!
			}

			T firstEPD = null;
			int pos = 0;
			for(T secondEPD : this.chronoData) { // Time gets earlier, number goes down
				if (firstEPD != null) {
					if (firstEPD.getTimestamp() >= serverTimeToUse && secondEPD.getTimestamp() <= serverTimeToUse) {
						/*if (cleardown) {
								this.cleardown(pos+4);
							}*/
						return secondEPD; // return the first one, chronologically
					}
				}
				firstEPD = secondEPD;
				pos++;
			}
			//throw new RuntimeException("Should not get here!");
		}
		//}
		return null;

	}


	private void cleardown(long currentTime) {
		/*if (num > 0) {
			while (this.chronoData.size() > num) {
				this.chronoData.removeLast();
			}
		}*/
		long thresh = currentTime - this.historyLength;
		while (this.chronoData.size() > 100) { // Keep at least X amount
			ITimeStamped epd = this.chronoData.getLast();
			if (epd.getTimestamp() < thresh) {
				this.chronoData.removeLast();
			} else {
				break;			
			}
		}

	}


	public void clear() {
		//synchronized (chronoData) {
		chronoData.clear();
		//}
	}


}
