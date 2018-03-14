package ssmith.astar;

public interface IAStarMapInterface {
	
	int getMapWidth();

	int getMapHeight();
	
	boolean isMapSquareTraversable(int x, int z);

	float getMapSquareDifficulty(int x, int z);

}
