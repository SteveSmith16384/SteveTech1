package com.scs.stetech1.components;

import com.scs.stetech1.server.AbstractGameServer;

public interface IProcessByServer {

	void processByServer(AbstractGameServer server, float tpf_secs); // todo - rename to processByServer?
	
}
