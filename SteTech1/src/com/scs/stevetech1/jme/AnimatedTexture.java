package com.scs.stevetech1.jme;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class AnimatedTexture {

	private List<StandardImage> images;
	private int imageNum = 0;

	public AnimatedTexture() {

	}


	public void loadImages(String pathAndFile) throws IOException {
		images = new ArrayList<>();

		ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
		File input = new File(pathAndFile);
		ImageInputStream stream = ImageIO.createImageInputStream(input);
		reader.setInput(stream);

		int count = reader.getNumImages(true);
		for (int index = 0; index < count; index++) {
			BufferedImage frame = reader.read(index);
			StandardImage pi = new StandardImage(59, 42, frame);
			images.add(pi);
		}
	}


	public StandardImage getNextImage() {
		if (this.imageNum >= this.images.size()) {
			this.imageNum = 0;
		}
		StandardImage si = this.images.get(this.imageNum);
		imageNum++;
		return si;
	}
}
