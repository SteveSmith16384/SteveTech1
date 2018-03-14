package ssmith.astar;

/**
 * @author stephen smith
 *
 */
public class AStarTest implements IAStarMapInterface {
	
	private int[][] map;

	/**
	 * 
	 */
	public AStarTest() {
		super();
		int[][] map2 = {
				{0, 0, 0, 0, 0},
				{1, 1, 1, 1, 0},
				{0, 0, 0, 1, 0},
				{0, 0, 1, 1, 0},
				{0, 0, 0, 0, 0}
		};
		this.map = map2;
		
		System.out.println("map=" + map2[0][3]);
		
		AStar s = new AStar(this);
		s.findPath(3, 0, 0, 0, false);
/*	  	s.DisplayPath(1, 1, 3, 3);
	  	s.DisplayPath(2, 4, 3, 12);*/
	}

	/* (non-Javadoc)
	 * @see ssmith.astar.IAStarMap#getMapWidth()
	 */
	public int getMapWidth() {
		return map.length;
	}

	/* (non-Javadoc)
	 * @see ssmith.astar.IAStarMap#getMapHeight()
	 */
	public int getMapHeight() {
		return map.length;
	}

	/* (non-Javadoc)
	 * @see ssmith.astar.IAStarMap#isMapSquareTraversable(int, int)
	 */
	public boolean isMapSquareTraversable(int x, int z) {
		return map[x][z] == 0;
	}

	public float getMapSquareDifficulty(int x, int z) {
		return 1;
	}

}
