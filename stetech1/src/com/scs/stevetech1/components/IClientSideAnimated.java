package com.scs.stevetech1.components;

import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;

public interface IClientSideAnimated {

	ChronologicalLookup<HistoricalAnimationData> getAnimList();
	
	//int getCurrentAnimCode();
	
	void setAnimCode(int s);
	
	void processAnimation(float tpf_secs);
	
}
