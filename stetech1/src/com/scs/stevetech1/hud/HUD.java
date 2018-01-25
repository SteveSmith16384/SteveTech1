package com.scs.stevetech1.hud;

import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.gui.TextArea;
import com.scs.stevetech1.server.Globals;

/*
 * Positioning text = the co-ords of BitmapText are for the top-left of the first line of text, and they go down from there.
 * 
 */
public class HUD extends Node implements IProcessByClient {

	private static ColorRGBA defaultColour = ColorRGBA.Black;
	
	public TextArea log_ta;
	public float hud_width, hud_height;
	private Camera cam;
	private Geometry damage_box;
	private ColorRGBA dam_box_col = new ColorRGBA(1, 0, 0, 0.0f);
	private boolean process_damage_box;
	private AbstractGameClient game;
	
	private BitmapText abilityGun, abilityOther, debugText, gameStatus, gameTime;

	public HUD(AbstractGameClient _game, BitmapFont font_small, Camera _cam) {
		super("HUD");

		game = _game;
		hud_width = _cam.getWidth();
		hud_height = _cam.getHeight();
		cam = _cam;

		super.setLocalTranslation(0, 0, 0);

		//this.addTargetter();

		if (Globals.DEBUG_HUD) {
			for (int i=0; i<100 ; i+=10) {
				BitmapText deleteme = new BitmapText(font_small, false);
				deleteme.setColor(ColorRGBA.White);
				deleteme.setLocalTranslation(i, i, 0);
				this.attachChild(deleteme);
				deleteme.setText("x" + i);
			}
		}

		abilityGun = new BitmapText(font_small, false);
		abilityGun.setColor(defaultColour);
		abilityGun.setLocalTranslation(10, hud_height-30, 0);
		this.attachChild(abilityGun);

		abilityOther = new BitmapText(font_small, false);
		abilityOther.setColor(defaultColour);
		abilityOther.setLocalTranslation(10, hud_height-45, 0);
		this.attachChild(abilityOther);

		debugText = new BitmapText(font_small, false);
		debugText.setColor(defaultColour);
		debugText.setLocalTranslation(10, hud_height-60, 0);
		this.attachChild(debugText);

		gameStatus = new BitmapText(font_small, false);
		gameStatus.setColor(defaultColour);
		gameStatus.setLocalTranslation(10, hud_height-75, 0);
		this.attachChild(gameStatus);

		gameTime = new BitmapText(font_small, false);
		gameTime.setColor(defaultColour);
		gameTime.setLocalTranslation(10, hud_height-90, 0);
		this.attachChild(gameTime);

		log_ta = new TextArea("log", font_small, 6, "Entities");
		log_ta.setColor(defaultColour);
		log_ta.setLocalTranslation(0, hud_height/2, 0);
		this.attachChild(log_ta);

		// Damage box
		{
			Material mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", this.dam_box_col);
			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			damage_box = new Geometry("damagebox", new Quad(hud_width, hud_height));
			damage_box.move(0, 0, 0);
			damage_box.setMaterial(mat);
			this.attachChild(damage_box);
		}

		/*if (Settings.DEBUG_HUD) {
			Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", new ColorRGBA(1, 1, 0, 0.5f));
			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			Geometry testBox = new Geometry("testBox", new Quad(w/2, h/2));
			testBox.move(10, 10, 0);
			testBox.setMaterial(mat);
			this.attachChild(testBox);

			/*Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			//mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			Texture t = game.getAssetManager().loadTexture("Textures/text/hit.png");
			//t.setWrap(WrapMode.Repeat);
			mat.setTexture("DiffuseMap", t);
			Geometry geom = new Geometry("Billboard", new Quad(w, h));
			geom.setMaterial(mat);
			geom.move(0, 0, 0);
			//geom.setQueueBucket(Bucket.Transparent);
			//geom.setLocalTranslation(-w/2, -h/2, 0);
			this.attachChild(geom);*/
		/*
			Picture pic = new Picture("HUD Picture");
			pic.setImage(game.getAssetManager(), "Textures/text/hit.png", true);
			pic.setWidth(w);
			pic.setHeight(h);
			//pic.setPosition(settings.getWidth()/4, settings.getHeight()/4);
			this.attachChild(pic);
		}*/


		this.updateGeometricState();

		this.setModelBound(new BoundingBox());
		this.updateModelBound();

		//_game.addEntity(this);

	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf) {
		if (process_damage_box) {
			this.dam_box_col.a -= (tpf/2);
			if (dam_box_col.a <= 0) {
				dam_box_col.a = 0;
				process_damage_box = false;
			}
		}

	}


	public void log(String s) {
		this.log_ta.addLine(s);
	}


	public void setDebugText(String s) {
		this.debugText.setText(s);
	}


	public void setGameStatus(String s) {
		this.gameStatus.setText(s);
	}


	public void setGameTime(String s) {
		this.gameTime.setText(s);
	}


	public void setAbilityGunText(String s) {
		this.abilityGun.setText(s);
	}


	public void setAbilityOtherText(String s) {
		this.abilityOther.setText(s);
	}


	public void showDamageBox() {
		process_damage_box = true;
		this.dam_box_col.a = .5f;
		this.dam_box_col.r = 1f;
		this.dam_box_col.g = 0f;
		this.dam_box_col.b = 0f;
	}


	public void showCollectBox() {
		process_damage_box = true;
		this.dam_box_col.a = .3f;
		this.dam_box_col.r = 0f;
		this.dam_box_col.g = 1f;
		this.dam_box_col.b = 1f;
	}


	private void addTargetter() {
		Picture targetting_reticule = new Picture("HUD Picture");
		targetting_reticule.setImage(game.getAssetManager(), "Textures/circular_recticle.png", true);
		float crosshairs_w = cam.getWidth()/10;
		targetting_reticule.setWidth(crosshairs_w);
		float crosshairs_h = cam.getHeight()/10;
		targetting_reticule.setHeight(crosshairs_h);
		this.setLocalTranslation((cam.getWidth() - crosshairs_w)/2, (cam.getHeight() - crosshairs_h)/2, 0);
		this.attachChild(targetting_reticule);

		//this.targetting_reticules.add(targetting_reticule);
	}
}
