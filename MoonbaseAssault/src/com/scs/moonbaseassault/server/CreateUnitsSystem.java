package com.scs.moonbaseassault.server;

import com.scs.moonbaseassault.entities.AbstractAISoldier;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractServerAvatar;

import ssmith.util.RealtimeInterval;

/**
 * Notes:
 * * Players are worth more than AI
 * * Need more defenders since they have to cover a bigger area
 * 
 */
public class CreateUnitsSystem {

	private static int nextSoldierNum = 1; 
	private int numDefenders, numAttackers;
	private RealtimeInterval recalcNumUnitsInterval = new RealtimeInterval(1000 * 10);
	private MoonbaseAssaultServer server;

	public CreateUnitsSystem(MoonbaseAssaultServer _server) {
		server = _server;
	}


	public void process() {
		if (recalcNumUnitsInterval.hitInterval()) {

			numAttackers = 0;
			this.numDefenders = 0;

			for (int i=0 ; i<server.entitiesForProcessing.size() ; i++) {
				IEntity e = server.entitiesForProcessing.get(i);
				if (e instanceof AbstractServerAvatar) {
					numAttackers += 3; // Players are always attackers, and worth more
				} else if (e instanceof AbstractAISoldier) {
					AbstractAISoldier ai = (AbstractAISoldier)e;
					if (ai.side == 1) {
						numAttackers += 2;
					} else if (ai.side == 2) {
						this.numDefenders++;
					} else {
						throw new RuntimeException("Invalid side: " + ai.side);
					}
				}
			}

			//if (this.numAttackers > this.numDefenders) {
				while (this.numDefenders <= this.numAttackers || numDefenders < 4) {
					server.addAISoldier(2, nextSoldierNum++);
					this.numDefenders++;
				}
			//} else if (this.numAttackers < this.numDefenders) {
				while (this.numAttackers < this.numDefenders || this.numAttackers < 4) {
					server.addAISoldier(1, nextSoldierNum++);
					this.numAttackers++;
				}
			//}

		}
	}

}
