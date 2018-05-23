package com.scs.moonbaseassault.client.hud;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import com.scs.moonbaseassault.server.MapLoader;
import com.scs.stevetech1.jme.PaintableImage;
import com.scs.stevetech1.server.Globals;

public class MapImageTexture extends PaintableImage {

	private static final float ALPHA = 1f; //0 = transparent

	private int[][] data;
	private Point player;
	private List<Point> units;
	private List<Point> computers;
	private int pixelSize; // How big each map square is on the scanner

	public MapImageTexture(int sizeInPixels, int _pixelSize) {
		super(sizeInPixels, sizeInPixels);

		pixelSize = Math.max(1, _pixelSize);

		refreshImage();
	}


	public void setMapData(int[][] _data) {
		data = _data;
		this.refreshImage();
	}


	public void setOtherData(Point _player, List<Point> _units, List<Point> _computers) {
		player =_player;
		units = _units;
		computers = _computers;
		this.refreshImage();
	}


	@Override
	public void paint(Graphics2D g) {
		g.setBackground(new Color(0f, 0f, 0f, 0f));
		g.clearRect(0, 0, getWidth(), getHeight());

		// Origin is bottom-left
		if (data != null) {
			// Map walls
			g.setColor(new Color(0f, 1f, 0f, ALPHA)); // green
			for (int y=0 ; y<data.length ; y++) {
				for (int x=0 ; x<data[y].length ; x++) {
					if (data[y][x] == MapLoader.WALL) {
						//g.fillRect((data.length-1-y)*pixelSize, (data[0].length-1-x)*pixelSize, pixelSize, pixelSize);
						paintSquare(g, x, y, 1);
					}
				}
			}

			/*if (Globals.DEBUG_HUD) {
				g.setColor(new Color(1f, 0f, 1f, 1f));
				//g.fillRect(0, 0, pixelSize, pixelSize);
				paintSquare(g, 0, 0);
				g.setColor(new Color(1f, 0f, 0f, 1f));
				//g.fillRect((data.length-1)*pixelSize, (data[0].length-1)*pixelSize, pixelSize, pixelSize);
				paintSquare(g, 2, 2);
			} */

			// Units
			if (units != null) {
				g.setColor(new Color(1f, 0f, 0f, ALPHA)); // Red

				for (int i=0 ; i<units.size() ; i++) {
					Point p = units.get(i);
					//g.fillRect((data.length-p.y)*pixelSize, (data.length-p.x)*pixelSize, pixelSize, pixelSize);
					paintSquare(g, p.x, p.y, 3);
				}			
			}

			// Computers
			if (computers != null) {
				g.setColor(new Color(1f, 1f, 1f, ALPHA)); // White
				for (int i=0 ; i<computers.size() ; i++) {
					Point p = computers.get(i);
					//g.fillRect((data.length-p.y)*pixelSize, (data.length-p.x)*pixelSize, pixelSize, pixelSize);
					paintSquare(g, p.x, p.y, 2);
				}			
			}

			// Player
			if (player != null) {
				g.setColor(new Color(1f, 1f, 0f, ALPHA)); // Yellow
				paintSquare(g, player.x, player.y, 3);
			}				
		}

	}

	private void paintSquare(Graphics2D g, int mx, int my, int pxlSizeMult) {
		int x = (data.length-1-my)*pixelSize;
		int y = (data[0].length-1-mx)*pixelSize;
		g.fillRect(x, y, pixelSize*pxlSizeMult, pixelSize*pxlSizeMult);

	}


}
