package com.scs.stetech1.components;

import com.scs.stetech1.server.ServerMain;

public interface IProcessByServer {

	void process(ServerMain server, float tpf_secs);
	
}
