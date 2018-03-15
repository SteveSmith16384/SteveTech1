package com.scs.moonbaseassault.client.hud;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import com.scs.moonbaseassault.server.MapLoader;

public class MapImageTexture extends PaintableImage {

	private int[][] data;
	public List<Point> units;

	public MapImageTexture(int w, int h) {
		super(w, h);
		refreshImage();
	}


	public void setMapData(int[][] _data) {
		data = _data;
		this.refreshImage();
	}
	
	
	public void setUnits(List<Point> _units) {
		units = _units;
		this.refreshImage();
	}


	public void paint(Graphics2D g) {
		g.setBackground(new Color(0f, 0f, 0f, .4f));
		g.clearRect(0, 0, getWidth(), getHeight());

		if (data != null) {
			g.setColor(new Color(1f, 1f, 1f, .8f));

			for (int y=0 ; y<data.length ; y++) {
				for (int x=0 ; x<data[y].length ; x++) {
					if (data[y][x] == MapLoader.WALL) {
						g.fillRect(data.length-y, data.length-x, 1, 1);
					}
				}
			}			
			if (units != null) {
				g.setColor(new Color(1f, 0f, 0f, 1f));

				for (int i=0 ; i<units.size() ; i++) {
					Point p = units.get(i);
					g.fillRect(data.length-p.y, p.x, 1, 1);
				}			
			}
		}
	}

}
