package com.scs.moonbaseassault.client.hud;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

public class HUDMapImage extends Picture {

	public HUDMapImage(AssetManager assetManager) {
		super("HUDMapImage");
		
		PaintedGauge gaugeImage = new PaintedGauge();

		gaugeImage.setValue(99);

		Texture2D texture = new Texture2D(64, 64, Format.ABGR8);

		texture.setMinFilter(Texture.MinFilter.Trilinear);

		texture.setMagFilter(Texture.MagFilter.Bilinear);

		texture.setImage(gaugeImage);

		
		this.setTexture(assetManager, texture, true);



		//this.setWidth(64);

		//this.setHeight(64);

	}

}
