package com.scs.moonbaseassault.client.hud;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import com.scs.moonbaseassault.server.MapLoader;
import com.scs.stevetech1.server.Globals;

public class MapImageTexture extends PaintableImage {

	private static final float ALPHA = 0.9f;

	private int[][] data;
	private Point player;
	private List<Point> units;
	private List<Point> computers;
	private int pixelSize; // How big each map square is on the scanner

	public MapImageTexture(int sizeInPixels, int _pixelSize) {
		super(sizeInPixels, sizeInPixels);

		pixelSize = _pixelSize;

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


	public void paint(Graphics2D g) {
		g.setBackground(new Color(0f, 0f, 0f, .0f));
		g.clearRect(0, 0, getWidth(), getHeight());

		if (data != null) {
			// Map walls
			g.setColor(new Color(0f, 0f, 1f, ALPHA));
			for (int y=0 ; y<data.length ; y++) {
				for (int x=0 ; x<data[y].length ; x++) {
					if (data[y][x] == MapLoader.WALL) {
						g.fillRect((data.length-y)*pixelSize, (data.length-x)*pixelSize, pixelSize, pixelSize);
					}
				}
			}

			// Units
			if (units != null) {
				g.setColor(new Color(1f, 1f, 1f, ALPHA));

				for (int i=0 ; i<units.size() ; i++) {
					Point p = units.get(i);
					g.fillRect((data.length-p.y)*pixelSize, (data.length-p.x)*pixelSize, pixelSize, pixelSize);
				}			
			}

			if (Globals.DEBUG_HUD) {
				g.setColor(new Color(0f, 0f, 1f, .5f));
				g.fillRect(78, 84, pixelSize, pixelSize);
			}  else {
				// Computers
				if (computers != null) {
					g.setColor(new Color(0f, 1f, 1f, ALPHA));

					for (int i=0 ; i<computers.size() ; i++) {
						Point p = computers.get(i);
						g.fillRect((data.length-p.y)*pixelSize, (data.length-p.x)*pixelSize, pixelSize, pixelSize);
					}			
				}

				// Player
				if (player != null) {
					g.setColor(new Color(0f, 1f, 0f, ALPHA));
					g.fillRect((data.length-player.y)*pixelSize, (data.length-player.x)*pixelSize, pixelSize, pixelSize);
				}				

			}

		}
	}

}
