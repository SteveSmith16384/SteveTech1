package com.scs.stevetech1.jme;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class StandardImage extends PaintableImage implements ImageObserver {
	
	private BufferedImage image;

	public StandardImage(int width, int height, BufferedImage _image) {
		super(width, height);
		
		image = _image;
		
		this.refreshImage();
	}
	
	
	@Override
	public void paint(Graphics2D graphicsContext) {
		graphicsContext.drawImage(image, 0, 0, this.width, this.height, this);
		
	}


	@Override
	public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
		return false;
	}

}
