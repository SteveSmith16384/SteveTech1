package com.scs.stevetech1.systems.server;

import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;
import ssmith.util.RealtimeInterval;

public class ServerPingSystem {

	protected RealtimeInterval sendPingInterval = new RealtimeInterval(Globals.PING_INTERVAL_MS);

	private AbstractGameServer server;
	private int prevPingCode = -1;
	private int randomPingCode = NumberFunctions.rnd(0,  999999);

	public ServerPingSystem(AbstractGameServer _server) {
		server = _server;
	}


	public void process() {
		if (sendPingInterval.hitInterval()) {
			prevPingCode = randomPingCode;
			randomPingCode = NumberFunctions.rnd(0,  999999);
			server.sendMessageToAll_AreYouSure(new PingMessage(true, randomPingCode));
		}

	}


	public void sendPingToClient(ClientData client) {
		server.gameNetworkServer.sendMessageToClient(client, new PingMessage(true, this.randomPingCode));		
	
	}
	
	
	public void handleMessage(PingMessage pingMessage, ClientData client) {
		if (pingMessage.s2c) {
			try {
				// Check code
				if (pingMessage.randomCode == this.randomPingCode || pingMessage.randomCode == this.prevPingCode) {
					try {
						long rttDuration = System.currentTimeMillis() - pingMessage.originalSentTime;
						if (client.playerData != null) {
							client.playerData.pingRTT = client.pingCalc.add(rttDuration);
							client.serverToClientDiffTime = pingMessage.responseSentTime - pingMessage.originalSentTime - (client.playerData.pingRTT/2); // If running on the same server, this should be 0! (or close enough)
							/*todo - re-add if ((client.playerData.pingRTT/2) + sendEntityUpdatesInterval.getInterval() > clientRenderDelayMillis) {
								Globals.p("Warning: client ping is longer than client render delay!");
							}*/
						}
					} catch (NullPointerException ex) {
						Globals.HandleError(ex);
					}
				} else {
					Globals.pe("Unexpected ping response code:" + pingMessage.randomCode);
				}
			} catch (NullPointerException npe) {
				Globals.HandleError(npe);
			}
		} else {
			// Send it back to the client
			pingMessage.responseSentTime = System.currentTimeMillis();
			server.gameNetworkServer.sendMessageToClient(client, pingMessage);
		}
	}

}
