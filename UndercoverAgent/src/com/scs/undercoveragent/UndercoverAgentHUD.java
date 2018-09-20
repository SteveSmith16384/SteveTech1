package com.scs.undercoveragent;

import java.util.ArrayList;
import java.util.List;

import com.atr.jme.font.TrueTypeFont;
import com.atr.jme.font.TrueTypeMesh;
import com.atr.jme.font.asset.TrueTypeKeyMesh;
import com.atr.jme.font.asset.TrueTypeLoader;
import com.atr.jme.font.shape.TrueTypeContainer;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.Style;
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
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.gui.TextArea;

import ssmith.util.RealtimeInterval;

/*
 * Positioning text = the co-ords of BitmapText are for the top-left of the first line of text, and they go down from there.
 * 
 */
public class UndercoverAgentHUD extends Node {

	private static final int MAX_LINES = 5;

	private RealtimeInterval showGameTimeInterval = new RealtimeInterval(1000);

	private List<String> logLines = new ArrayList<String>();

	private Camera cam;
	private Geometry damage_box;
	private ColorRGBA dam_box_col = new ColorRGBA(1, 0, 0, 0.0f);
	private boolean process_damage_box;
	private AbstractGameClient game;
	private TrueTypeContainer gameStatus, gameTime, healthText, scoreText, pingText, numPlayers, logText;

	public UndercoverAgentHUD(AbstractGameClient _game, Camera _cam) { 
		super("HUD");

		game = _game;
		cam = _cam;

		_game.getAssetManager().registerLoader(TrueTypeLoader.class, "ttf");
		float fontSize = cam.getWidth() / 40; 
		TrueTypeKeyMesh ttkSmall = new TrueTypeKeyMesh("Fonts/SF Distant Galaxy.ttf", Style.Plain, (int)fontSize);
		TrueTypeFont ttfSmall = (TrueTypeMesh)_game.getAssetManager().loadAsset(ttkSmall);
		TrueTypeKeyMesh ttkLarge = new TrueTypeKeyMesh("Fonts/SF Distant Galaxy.ttf", Style.Plain, (int)fontSize*2);
		TrueTypeFont ttfLarge = (TrueTypeMesh)_game.getAssetManager().loadAsset(ttkLarge);
		float lineSpacing = cam.getHeight() / 30;

		super.setLocalTranslation(0, 0, 0);

		healthText = ttfLarge.getFormattedText(new StringContainer(ttfLarge, "Hello World"), ColorRGBA.Green);
		healthText.setLocalTranslation(10, this.cam.getHeight()-20, 0);
		this.attachChild(healthText);

		scoreText = ttfLarge.getFormattedText(new StringContainer(ttfLarge, "Score:"), ColorRGBA.Green);
		scoreText.setLocalTranslation(10, this.cam.getHeight()-(ttfLarge.getScaledLineHeightInt()*2), 0);
		this.attachChild(scoreText);

		float xPos = cam.getWidth() * .6f;
		float yPos = cam.getHeight() - 20;//lineSpacing;

		yPos -= lineSpacing;
		gameStatus = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Game Status:"), ColorRGBA.Green);
		gameStatus.setLocalTranslation(xPos, yPos, 0);
		gameStatus.updateGeometry();
		this.attachChild(gameStatus);

		yPos -= lineSpacing;
		gameTime = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Hello World"), ColorRGBA.Green);
		gameTime.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(gameTime);
		/*
		yPos -= LINE_SPACING;
		abilityGun = ttf.getFormattedText(new StringContainer(ttf, "Hello World"), ColorRGBA.Green);
		///abilityGun.setColor(defaultColour);
		abilityGun.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(abilityGun);
		 *//*
		yPos -= LINE_SPACING;
		abilityOther = new BitmapText(font_small, false);
		abilityOther.setColor(defaultColour);
		abilityOther.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(abilityOther);
		  */
		yPos -= lineSpacing;
		numPlayers = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Hello World"), ColorRGBA.Green);
		numPlayers.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(numPlayers);

		yPos -= lineSpacing;
		pingText = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Hello World"), ColorRGBA.Green);
		pingText.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(pingText);

		logText = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Hello World"), ColorRGBA.Green);
		logText.setLocalTranslation(10, cam.getHeight() * 0.25f, 0);
		this.attachChild(logText);

		// Damage box
		{
			Material mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", this.dam_box_col);
			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			damage_box = new Geometry("damagebox", new Quad(cam.getWidth(), cam.getHeight()));
			damage_box.move(0, 0, 0);
			damage_box.setMaterial(mat);
			this.attachChild(damage_box);
		}

		this.updateGeometricState();

		this.setModelBound(new BoundingBox());
		this.updateModelBound();

	}


	public void processByClient(AbstractGameClient client, float tpf) {
		if (showGameTimeInterval.hitInterval()) {
			if (client.gameData != null) {
				this.setGameStatus(SimpleGameData.getStatusDesc(client.gameData.getGameStatus()));
				if (client.gameData.getGameStatus() != SimpleGameData.ST_WAITING_FOR_PLAYERS) {
					this.setGameTime(client.gameData.getTime(client.serverTime));
				} else {
					this.setGameTime("");
				}
				if (client.playersList != null) {
					this.setNumPlayers(client.playersList.size());
				}
			}
			this.setPing(client.pingRTT);

			if (client.currentAvatar != null) {
				this.setHealthText((int)client.currentAvatar.getHealth());
			}
		}

		if (process_damage_box) {
			this.dam_box_col.a -= (tpf/2);
			if (dam_box_col.a <= 0) {
				dam_box_col.a = 0;
				process_damage_box = false;
			}
		}

	}


	public void appendToLog(String s) {
		this.logLines.add(s);
		while (this.logLines.size() > MAX_LINES) {
			this.logLines.remove(0);
		}
		StringBuilder str = new StringBuilder();
		for(String line : this.logLines) {
			str.append(line + "\n");
		}
		this.logText.setText(str.toString());
		this.logText.updateGeometry();
	}

	/*
	public void setDebugText(String s) {
		this.debugText.setText(s);
	}
	 */

	public void setGameStatus(String s) {
		this.gameStatus.setText(s);
		this.gameStatus.updateGeometry();
	}


	public void setGameTime(String s) {
		this.gameTime.setText(s);
		this.gameTime.updateGeometry();
	}

	/*
	public void setAbilityGunText(String s) {
		this.abilityGun.setText(s);
	}


	public void setAbilityOtherText(String s) {
		this.abilityOther.setText(s);
	}
	 */

	public void setHealthText(int s) {
		this.healthText.setText("Health: " + s);
		this.healthText.updateGeometry();
	}


	public void setScoreText(int s) {
		this.scoreText.setText("Score: " + s);
		this.scoreText.updateGeometry();
	}


	public void setPing(long i) {
		this.pingText.setText("Ping: " + i);
		this.pingText.updateGeometry();
	}


	public void setNumPlayers(int i) {
		this.numPlayers.setText("Num Players: " + i);
		this.numPlayers.updateGeometry();
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


}
