package com.scs.moonbaseassault.client;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.scs.stevetech1.jme.PaintableImage;

/**
 * origin is top-left
 * Rows (from top):
 * 0 - Skin tone
 * 1 - Eyes and eyebrows
 * 2 - Hair
 * 3 - Shirt
 * 4 - Trousers
 *
 */
public class SoldierTexture {
	
	private static final int SIZE = 32;

	public SoldierTexture() {
		super();
	}
	
	
	public static Texture getTexture(int side) {
		PaintableImage pi = new PaintableImage(SIZE, SIZE) {
			
			@Override
			public void paint(Graphics2D g) {
				for (int row=0 ; row<5 ; row++) {
					switch (row) {
					case 0: // Skin / trousers
						switch (side) {
						case 1:
							g.setColor(Color.yellow.darker());
							break;
						case 2:
							g.setColor(Color.green.darker());
							break;
						default:
							throw new RuntimeException("Todo");
						}
						break;
						
					case 1: // Eyes and brows / top
						switch (side) {
						case 1:
							g.setColor(Color.yellow);
							break;
						case 2:
							g.setColor(Color.green);
							break;
						default:
							throw new RuntimeException("Todo");
						}
						break;
						
					case 2: // Hair
						g.setColor(getRandomHairColour());
						break;
						
					case 3: // Shirt
						g.setColor(Color.LIGHT_GRAY);
						break;

					case 4:
						g.setColor(getRandomSkinColour());
						break;
					}
					
					int sy = getRowStart(row);
					int ey = getRowStart(row+1)-1;
					g.fillRect(0, sy, SIZE, ey);
				}
			}
			
		};
		
		pi.refreshImage();
		return new Texture2D(pi);
	}
	
	
	private static int getRowStart(int row) {
		switch (row) {
		case 0:
			return 0;
		case 1:
			return 10;
		case 2:
			return 15;
		case 3:
			return 21;
		case 4:
			return 26;
		case 5:
			return 31;
			default:
				throw new RuntimeException("Todo");
		}
	}
	
	
	private static int getRowStart_OLD(int row) {
		switch (row) {
		case 0:
			return 0;
		case 1:
			return 6;
		case 2:
			return 11;
		case 3:
			return 17;
		case 4:
			return 22;
		case 5:
			return 33;
			default:
				throw new RuntimeException("Todo");
		}
	}
	
	
	private static Color getRandomHairColour() {
		return Color.DARK_GRAY;
	}
	

	private static Color getRandomSkinColour() {
		return Color.pink; // trousers
	}
	
}

