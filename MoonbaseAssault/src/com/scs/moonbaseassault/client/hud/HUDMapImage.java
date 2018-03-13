package com.scs.moonbaseassault.client.hud;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

public class HUDMapImage extends Picture {

	public MapImage mapImage;
	
	public HUDMapImage(AssetManager assetManager, int w, int h) {
		super("HUDMapImage");
		
		mapImage = new MapImage(w, h);
		Texture2D texture = new Texture2D(w, h, Format.ABGR8);
		texture.setMinFilter(Texture.MinFilter.Trilinear);
		texture.setMagFilter(Texture.MagFilter.Bilinear);
		texture.setImage(mapImage);
		this.setTexture(assetManager, texture, true);
	}

}
