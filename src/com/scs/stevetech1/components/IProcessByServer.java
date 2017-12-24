package com.scs.stevetech1.components;

import com.scs.stevetech1.server.AbstractGameServer;

public interface IProcessByServer {

	void processByServer(AbstractGameServer server, float tpf_secs);
	
}
