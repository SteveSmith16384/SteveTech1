package com.scs.moonbaseassault.server;

import com.scs.moonbaseassault.entities.AbstractAISoldier;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractServerAvatar;

import ssmith.util.RealtimeInterval;

/**
 * Notes:
 * - Players are worth more than AI
 * - Need more defenders since they have to cover a bigger area
 * - Minimum of 8 defenders, minimum of 4 attackers
 * - Minimum of double the defenders than attackers
 */
public class CreateUnitsSystem {

	private static int nextSoldierNum = 1; 
	private RealtimeInterval recalcNumUnitsInterval = new RealtimeInterval(1000 * 10);
	private MoonbaseAssaultServer server;

	public CreateUnitsSystem(MoonbaseAssaultServer _server) {
		server = _server;
	}


	public void process() {
		if (recalcNumUnitsInterval.hitInterval()) {

			int numAttackers = 0;
			int numDefenders = 0;
			//float requiredAttackers = 0;
			//int requiredDefenders = 0;

			for (int i=0 ; i<server.entitiesForProcessing.size() ; i++) {
				IEntity e = server.entitiesForProcessing.get(i);
				if (e instanceof AbstractServerAvatar) {
					numAttackers += 2;
					//requiredDefenders += 3; // Players are always attackers, and worth more
				} else if (e instanceof AbstractAISoldier) {
					AbstractAISoldier ai = (AbstractAISoldier)e;
					if (ai.side == 1) { // Attacker
						numAttackers++;
						//requiredDefenders += 2;
					} else if (ai.side == 2) { // Defender
						numDefenders++;
						//requiredAttackers += 0.5f;
					} else {
						throw new RuntimeException("Invalid side: " + ai.side);
					}
				}
			}

			// Create attackers
			while (numAttackers < 5) { // 2 players, one AI?
				server.addAISoldier(1, nextSoldierNum++);
				numAttackers++;
			}

			// Create defenders
			while (numDefenders < numAttackers*2 || numDefenders < 8) {
				server.addAISoldier(2, nextSoldierNum++);
				numDefenders++;
			}

		}
	}

}
