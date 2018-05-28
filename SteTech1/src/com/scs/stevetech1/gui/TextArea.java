package com.scs.stevetech1.gui;

import java.util.ArrayList;
import java.util.List;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

public class TextArea extends BitmapText {

	private volatile List<String> lines;
	private int max;

	public TextArea(String name, BitmapFont guiFont, int _max, String text) {
		super(guiFont, false);

		this.setName(this.getName() + ": " + name);
		this.setSize(guiFont.getCharSet().getRenderedSize());
		this.setColor(ColorRGBA.LightGray);
		this.setText(text);

		lines = new ArrayList<String>(max*2);
		max = _max;

	}


	public void addLine(String s) {
		synchronized (lines) {
			lines.add(s);
			if (max > 0) {
				while (lines.size() > max) {
					lines.remove(0);
				}
			}
		}
		this.setText();
	}


	private void setText() {
		StringBuffer str = new StringBuffer();
		synchronized (lines) {
			for (int i=0 ; i<lines.size() ; i++) {
				str.append(lines.get(i)+ "\n");
			}
		}
		super.setText(str.toString());

	}

}