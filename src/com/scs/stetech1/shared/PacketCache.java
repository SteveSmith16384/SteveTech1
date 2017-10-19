package com.scs.stetech1.shared;

import java.util.HashMap;
import java.util.Iterator;

import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.server.Settings;

public class PacketCache {

	private HashMap<Long, MyAbstractMessage> msgs = new HashMap<>();

	public PacketCache() {

	}


	public void add(MyAbstractMessage m) {
		if (!m.requiresAck) {
			throw new RuntimeException("todo");
		}
		
		synchronized(msgs) {
			msgs.put(m.msgId, m);
		}
		if (msgs.size() > 100) {
			Settings.p("Lots of messages! " + msgs.size());
		}
	}

	
	public boolean hasBeenAckd(long id) {
		return this.msgs.containsKey(id);
	}
	

	public void acked(long id) {
		synchronized(msgs) {
			msgs.remove(id);
		}
	}


	public Iterator<MyAbstractMessage> getMsgs() {
		return this.msgs.values().iterator();
	}


/*	public void sendAll(MessageConnection conn) {
		synchronized(msgs) {
			//Iterator<MyAbstractMessage> it = this.msgs.values().iterator();
			for (MyAbstractMessage m : this.msgs.values()) {
				conn.send(m);
			}
		}
	}
*/
}
