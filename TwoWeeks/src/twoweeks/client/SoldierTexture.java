package twoweeks.client;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.scs.stevetech1.jme.PaintableImage;

public class SoldierTexture {
	
	private static final int SIZE = 32;

	public SoldierTexture() {
		super();
	}
	
	
	public static Texture getTexture() {
		PaintableImage pi = new PaintableImage(SIZE, SIZE) {
			
			@Override
			public void paint(Graphics2D g) {
				for (int row=0 ; row<4 ; row++) {
					switch (row) {
					case 0:
						g.setColor(getRandomHairColour());//Color.black);
						break;
					case 1:
						g.setColor(Color.white);
						break;
					case 2:
						g.setColor(Color.green);
						break;
					case 3:
						g.setColor(Color.gray);
						break;
					}
					g.fillRect(0, row*(SIZE/4), SIZE, (row+1)*(SIZE/4));
				}
			}
			
		};
		
		pi.refreshImage();
		return new Texture2D(pi);
	}
	
	
	private static Color getRandomHairColour() {
		return Color.black;
	}
	
}
