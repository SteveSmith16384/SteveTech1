package com.scs.moonbaseassault.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.moonbaseassault.entities.SlidingDoor;
import com.scs.stevetech1.server.Globals;

public class MapLoader {

	private static final int HANDLED = 0;
	private static final int INT_FLOOR = 1;
	private static final int EXT_FLOOR = 2;
	private static final int WALL = 3;
	private static final int DOOR_LR = 4;
	private static final int DOOR_UD = 5;
	private static final int COMPUTER = 6;

	private int mapCode[][];
	private int mapsize;
	private int totalWalls;
	private MoonbaseAssaultServer moonbaseAssaultServer;
	public int scannerData[][];

	public MapLoader(MoonbaseAssaultServer _moonbaseAssaultServer) {
		super();

		moonbaseAssaultServer = _moonbaseAssaultServer;
	}


	public void loadMap(String s) throws FileNotFoundException, IOException, URISyntaxException {
		String text = new String(Files.readAllBytes(Paths.get(getClass().getResource(s).toURI())));
		String[] lines = text.split(System.lineSeparator());

		mapsize = Integer.parseInt(lines[0]);
		mapCode = new int[mapsize][mapsize];

		for (int y=1 ; y<lines.length ; y++) { // Skip line 1
			String line = lines[y];
			String[] tokens = line.split(",");
			for (int x=0 ; x<tokens.length ; x++) {
				String cell = tokens[x];
				String[] subtokens = cell.split("\\|");  // FLOOR:1|DEPLOY:2|
				for(String part : subtokens) {
					String stringAndCode[] = part.split(":");
					if (stringAndCode[0].equals("WALL")) {
						mapCode[x][y-1] = WALL;
					} else if (stringAndCode[0].equals("COMP")) {
						mapCode[x][y-1] = COMPUTER;
					} else if (stringAndCode[0].equals("DOOR")) {
						if (stringAndCode[1].equals("1")) {
							mapCode[x][y-1] = DOOR_UD;
						} else if (stringAndCode[1].equals("2")) {
							mapCode[x][y-1] = DOOR_LR;
						}
					} else if (stringAndCode[0].equals("FLOOR")) {
						if (stringAndCode[1].equals("2")) {
							mapCode[x][y-1] = EXT_FLOOR;
						} else {
							mapCode[x][y-1] = INT_FLOOR;
						}
					}					
				}
			}
		}
		
		// Copy for scanner data
		scannerData = new int[mapsize][mapsize];
		for (int y=0 ; y<mapsize ; y++) {
			for (int x=0 ; x<mapsize ; x++) {
				scannerData[x][y] = mapCode[x][y];
			}
		}

		// print map
		for (int y=0 ; y<mapsize ; y++) {
			for (int x=0 ; x<mapsize ; x++) {
				if (mapCode[x][y] == WALL) {
					System.out.print("X");
				} else if (mapCode[x][y] == INT_FLOOR) {
					System.out.print(".");
				} else if (mapCode[x][y] == EXT_FLOOR) {
					System.out.print(":");
				} else if (mapCode[x][y] == COMPUTER) {
					System.out.print("C");
				} else if (mapCode[x][y] == DOOR_LR) {
					System.out.print("L");
				} else if (mapCode[x][y] == DOOR_UD) {
					System.out.print("U");
				} else {
					System.out.print(" ");
				}					
			}
			System.out.println("");
		}


		// Generate map!
		totalWalls = 0;

		{
			int y = 0;
			while (y < mapsize) {
				int x = 0;
				while (x < mapsize-1) {
					if (mapCode[x][y] == WALL && mapCode[x+1][y] == WALL) {
						checkForHorizontalWalls(x, y);
					}				
					x++;
				}						
				y++;
			}
		}

		{
			// Vertical walls
			int y = 0;
			while (y < mapsize-1) {
				int x = 0;
				while (x < mapsize) {
					if (mapCode[x][y] == WALL) {// && handled[x][y+1] == WALL) {
						checkForVerticalWalls(x, y);
					}				
					x++;
				}						
				y++;
			}
		}

		// Doors && comps
		{
			for (int y=0 ; y<mapsize ; y++) {
				for (int x=0 ; x<mapsize ; x++) {
					if (mapCode[x][y] == DOOR_LR) {
						SlidingDoor door = new SlidingDoor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x, 0, y, 1, MoonbaseAssaultServer.CEILING_HEIGHT, "Textures/door_lr.png", 0);
						moonbaseAssaultServer.actuallyAddEntity(door);
					} else if (mapCode[x][y] == DOOR_UD) {
						SlidingDoor door = new SlidingDoor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x, 0, y, 1, MoonbaseAssaultServer.CEILING_HEIGHT, "Textures/door_lr.png", 270);
						moonbaseAssaultServer.actuallyAddEntity(door);
					} else if (mapCode[x][y] == COMPUTER) {
						Computer comp = new Computer(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x, 0, y, "Textures/computerconsole2.jpg");
						moonbaseAssaultServer.actuallyAddEntity(comp);
					}
				}
			}
		}

		doCeilings();
		
		// Place floor & ceiling last
		Floor floor = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 0, 0, 0, mapsize, .5f, mapsize, "Textures/escape_hatch.jpg");
		moonbaseAssaultServer.actuallyAddEntity(floor);

		/*		Floor ceiling = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 0, MoonbaseAssaultServer.CEILING_HEIGHT, 0, mapsize, .5f, mapsize, "Textures/moonbase_ceiling.png");
		moonbaseAssaultServer.actuallyAddEntity(ceiling);
		 */


		Globals.p("Finished.  Created " + this.totalWalls + " walls");
	}


	private void doCeilings() {
		// Ceilings
		boolean found = true;
		while (found) {
			found = false;
			for (int y=0 ; y<mapsize ; y++) {
				for (int x=0 ; x<mapsize ; x++) {
					if (mapCode[x][y] == INT_FLOOR) {
						found = true;
						ceilings(x, y);
						//return;
					}
				}
			}
		}

	}
	
	
	private void ceilings(int sx, int sy) {
		// Go across
		int ex;
		for (ex=sx ; ex<mapsize ; ex++) {
			if (mapCode[ex][sy] != INT_FLOOR) {
				break;
			}
			mapCode[ex][sy] = HANDLED;
		}
		//ex--;
		// Go cover rect
		boolean breakout = false;
		int ey;
		for (ey=sy+1 ; ey<mapsize ; ey++) {
			for (int x=sx ; x<=ex ; x++) {
				if (mapCode[x][sy] != INT_FLOOR) {
					breakout = true;
					break;
				}
				//mapCode[x][sy] = HANDLED;
			}
			if (breakout) {
				break;
			}
		}
		//ey--;
		//Floor ceiling = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx, MoonbaseAssaultServer.CEILING_HEIGHT+0.5f, sy, ey-sy, .5f, ex-sx, "Textures/moonbase_ceiling.png");
		Floor ceiling = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx, MoonbaseAssaultServer.CEILING_HEIGHT+0.5f, sy, ex-sx, .5f, ey-sy, "Textures/moonbase_ceiling.png");
		moonbaseAssaultServer.actuallyAddEntity(ceiling);
		//Globals.p("Added ceiling");

		// Mark area as handled
		for (int y=sy ; y<ey ; y++) {
			for (int x=sx ; x<ex ; x++) {
				mapCode[x][sy] = HANDLED;
			}
		}

	}


	private void checkForHorizontalWalls(int sx, int sy) {
		int x;
		for (x=sx ; x<mapsize ; x++) {
			if (mapCode[x][sy] != WALL) {
				break;
			}
			mapCode[x][sy] = HANDLED;
		}
		x--;
		//Globals.p("Creating wall at " + sx + ", " + sy + " length: " + (x-sx));
		MoonbaseWall wall = new MoonbaseWall(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx, 0f, sy, x-sx+1, MoonbaseAssaultServer.CEILING_HEIGHT, 1, "Textures/ufo2_03.png");
		moonbaseAssaultServer.actuallyAddEntity(wall);
		totalWalls++;
	}


	private void checkForVerticalWalls(int sx, int sy) {
		int y;
		for (y=sy ; y<mapsize ; y++) {
			if (mapCode[sx][y] != WALL) {
				break;
			}
			mapCode[sx][y] = HANDLED;
		}
		y--;
		//Globals.p("Creating wall at " + sx + ", " + sy + " length: " + (y-sy));
		MoonbaseWall wall = new MoonbaseWall(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx, 0f, sy, 1, MoonbaseAssaultServer.CEILING_HEIGHT, y-sy+1, "Textures/spacewall2.png");
		moonbaseAssaultServer.actuallyAddEntity(wall);
		totalWalls++;
	}

}

