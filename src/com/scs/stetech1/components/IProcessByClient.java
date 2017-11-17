package com.scs.stetech1.components;

import com.scs.stetech1.client.GenericClient;

public interface IProcessByClient {

	void process(GenericClient client, float tpf_secs);

}
