package com.scs.stevetech1.components;

import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;

public interface IAnimated {

	ChronologicalLookup<HistoricalAnimationData> getAnimList();
	
	Object getCurrentAnim();
	
	void setCurrentAnim(Object s);
	
}
