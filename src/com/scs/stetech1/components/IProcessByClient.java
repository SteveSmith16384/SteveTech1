package com.scs.stetech1.components;

import com.scs.stetech1.client.AbstractGameClient;

public interface IProcessByClient {

	void process(AbstractGameClient client, float tpf_secs);

}
