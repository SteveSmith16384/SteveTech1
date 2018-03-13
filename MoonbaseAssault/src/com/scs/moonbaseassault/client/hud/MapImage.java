package com.scs.moonbaseassault.client.hud;

import java.awt.Color;
import java.awt.Graphics2D;

import com.scs.moonbaseassault.server.MapLoader;

public class MapImage extends PaintableImage {

	private int[][] data;

	public MapImage(int w, int h) {
		super(w, h);
		refreshImage();
	}


	public void setMapData(int[][] _data) {
		data = _data;
		this.refreshImage();
	}


	public void paint(Graphics2D g) {
		g.setBackground(new Color(0f, 0f, 0f, .4f));
		g.clearRect(0, 0, getWidth(), getHeight());

		if (data != null) {
			g.setColor(new Color(1f, 1f, 1f, .7f));
			for (int y=0 ; y<data.length ; y++) {
				for (int x=0 ; x<data[y].length ; x++) {
					if (data[y][x] == MapLoader.WALL) {
						g.fillRect(y, x, 1, 1);
					}
				}			
			}
		}
	}

}
