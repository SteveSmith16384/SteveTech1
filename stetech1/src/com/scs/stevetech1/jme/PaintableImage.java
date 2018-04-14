package com.scs.stevetech1.jme;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import com.jme3.texture.Image;

public abstract class PaintableImage extends Image {

	private BufferedImage backImg;
	private ByteBuffer scratch;
	
	public PaintableImage(int width, int height) {
		super();
		try {
			backImg = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

			setFormat(Format.ABGR8);
			setWidth(backImg.getWidth());
			setHeight(backImg.getHeight());
			scratch = ByteBuffer.allocateDirect(4 * backImg.getWidth() * backImg.getHeight());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}


	public void refreshImage() {
		Graphics2D g = backImg.createGraphics();
		paint(g);
		g.dispose();

		/* get the image data */
		byte data[] = (byte[]) backImg.getRaster().getDataElements(0, 0, backImg.getWidth(), backImg.getHeight(), null);
		scratch.clear();
		scratch.put(data, 0, data.length);
		scratch.rewind();
		setData(scratch);

	}

	public abstract void paint(Graphics2D graphicsContext);

}
