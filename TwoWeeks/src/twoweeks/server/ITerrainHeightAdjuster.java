package twoweeks.server;

import com.jme3.terrain.heightmap.AbstractHeightMap;

public interface ITerrainHeightAdjuster {

	void adjustHeight(AbstractHeightMap heightmap);
	
}
