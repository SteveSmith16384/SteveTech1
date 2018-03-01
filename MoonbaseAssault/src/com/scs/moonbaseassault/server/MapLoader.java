package com.scs.moonbaseassault.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MapLoader {

	private int handled[][];

	public MapLoader() {

	}


	public void loadMap(String s) throws FileNotFoundException, IOException, URISyntaxException {
		String text = new String(Files.readAllBytes(Paths.get(getClass().getResource(s).toURI())));
		String[] lines = text.split(System.lineSeparator());

		int size = Integer.parseInt(lines[0]);
		handled = new int[size][size];

		for (int y=1 ; y<lines.length ; y++) { // Skip line 1
			String line = lines[y];
			String[] tokens = line.split(",");
			for (int x=0 ; x<tokens.length ; x++) {
				String cell = tokens[x];
				String[] subtokens = cell.split("|");  // FLOOR:1|DEPLOY:2|
				for(String part : subtokens) {
					if (part.startsWith("WALL:")) {
						System.out.print("X");
					} else {
						System.out.print(" ");
					}					
				}
			}
			System.out.println("");
		}
	}

}
