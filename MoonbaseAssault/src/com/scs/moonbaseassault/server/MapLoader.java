package com.scs.moonbaseassault.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.stevetech1.server.Globals;

public class MapLoader {

	private static final int FLOOR = 1;
	private static final int WALL = 2;

	private int handled[][];
	private int mapsize;
	private int totalWalls;
	private MoonbaseAssaultServer moonbaseAssaultServer;

	public MapLoader(MoonbaseAssaultServer _moonbaseAssaultServer) {
		super();
		
		moonbaseAssaultServer = _moonbaseAssaultServer;
	}


	public void loadMap(String s) throws FileNotFoundException, IOException, URISyntaxException {
		String text = new String(Files.readAllBytes(Paths.get(getClass().getResource(s).toURI())));
		String[] lines = text.split(System.lineSeparator());

		mapsize = Integer.parseInt(lines[0]);
		handled = new int[mapsize][mapsize];

		for (int y=1 ; y<lines.length ; y++) { // Skip line 1
			String line = lines[y];
			String[] tokens = line.split(",");
			for (int x=0 ; x<tokens.length ; x++) {
				String cell = tokens[x];
				String[] subtokens = cell.split("\\|");  // FLOOR:1|DEPLOY:2|
				for(String part : subtokens) {
					if (part.startsWith("WALL:")) {
						handled[x][y-1] = WALL;
					}					
				}
			}
		}

		// print map
		for (int y=0 ; y<mapsize ; y++) { // Skip line 1
			for (int x=0 ; x<mapsize ; x++) {
				if (handled[x][y] == WALL) {
					System.out.print("X");
				} else {
					System.out.print(" ");
				}					
			}
			System.out.println("");
		}

		
		// Generate map!
		totalWalls = 0;
		
		int y = 0;
		while (y < mapsize) {
			int x = 0;
			while (x < mapsize-1) {
				if (handled[x][y] == WALL && handled[x+1][y] == WALL) {
					checkForHorizontalWalls(x, y);
				}				
				x++;
			}						
			y++;
		}

		// Vertical walls
		y = 0;
		while (y < mapsize-1) {
			int x = 0;
			while (x < mapsize) {
				if (handled[x][y] == WALL) {// && handled[x][y+1] == WALL) {
					checkForVerticalWalls(x, y);
				}				
				x++;
			}						
			y++;
		}

		// Place floor & ceiling last
		Floor floor = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 0, 0, 0, mapsize, .5f, mapsize, "Textures/bluemetal.png");
		moonbaseAssaultServer.actuallyAddEntity(floor);

		Floor ceiling = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 0, MoonbaseAssaultServer.CEILING_HEIGHT, 0, mapsize, .5f, mapsize, "Textures/moonbase_ceiling.png");
		moonbaseAssaultServer.actuallyAddEntity(ceiling);

		Globals.p("Finished.  Created " + this.totalWalls + " walls");
	}
	
	
	private void checkForHorizontalWalls(int sx, int sy) {
		int x;
		for (x=sx ; x<mapsize ; x++) {
			if (handled[x][sy] != WALL) {
				break;
			}
			handled[x][sy] = FLOOR;
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
			if (handled[sx][y] != WALL) {
				break;
			}
			handled[sx][y] = FLOOR;
		}
		y--;
		//Globals.p("Creating wall at " + sx + ", " + sy + " length: " + (y-sy));
		MoonbaseWall wall = new MoonbaseWall(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx, 0f, sy, 1, MoonbaseAssaultServer.CEILING_HEIGHT, y-sy+1, "Textures/spacewall2.png");
		moonbaseAssaultServer.actuallyAddEntity(wall);
		totalWalls++;
	}
	
}

