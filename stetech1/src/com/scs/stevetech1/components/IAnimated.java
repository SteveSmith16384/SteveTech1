package com.scs.stevetech1.components;

import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;

public interface IAnimated { // todo - rename to IClientSide....

	ChronologicalLookup<HistoricalAnimationData> getAnimList();
	
	String getCurrentAnimCode();
	
	void setCurrentAnimForCode(String s);
	
	void processAnimation(float tpf_secs);
	
}
