package com.scs.moonbaseassault.client.hud;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

public class HUDMapImage extends Picture {

	public MapImage gaugeImage;
	
	public HUDMapImage(AssetManager assetManager, int size) {
		super("HUDMapImage");
		
		gaugeImage = new MapImage(size);
		//gaugeImage.setValue(99);
		Texture2D texture = new Texture2D(64, 64, Format.ABGR8);
		texture.setMinFilter(Texture.MinFilter.Trilinear);
		texture.setMagFilter(Texture.MagFilter.Bilinear);
		texture.setImage(gaugeImage);
		this.setTexture(assetManager, texture, true);
	}

}
