package com.scs.moonbaseassault.client.hud;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

public class HUDMapImage extends Picture {

	public MapImageTexture mapImageTex;
	
	public HUDMapImage(AssetManager assetManager, int sizeInPixels, int mapSize) {
		super("HUDMapImage");
		
		int w = mapSize;
		int h = mapSize;
		
		mapImageTex = new MapImageTexture(sizeInPixels, sizeInPixels/mapSize);
		
		// Give this picture the texture of the map image
		Texture2D texture = new Texture2D(w, h, Format.ABGR8);
		texture.setMinFilter(Texture.MinFilter.Trilinear);
		texture.setMagFilter(Texture.MagFilter.Bilinear);
		texture.setImage(mapImageTex);
		this.setTexture(assetManager, texture, true);
	}

}
