package com.scs.stetech1.server;


public class Settings {

	public static final boolean NEON = true; // todo - remove
	public static final String VERSION = "0.01";
	public static final boolean SHOW_LOGO = false;
	public static final boolean RECORD_VID = false;
	public static final boolean USE_MODEL_FOR_PLAYERS = false;
	public static final boolean SHOW_FLASH_EXPLOSIONS = false;

	// DEBUG
	public static final boolean DEBUG_DEATH = false;
	public static final boolean DEBUG_HUD = false;

	// Game settings
	public static int NUM_COLLECTABLES = 0;
	public static int NUM_SECTORS = 3;
	
	// Our movement speed
	public static final float PLAYER_MOVE_SPEED = 3f;
	public static final float JUMP_FORCE = 8f;

	public static final float CAM_DIST = 50f;
	public static final int FLOOR_SECTION_SIZE = 12;
	public static final boolean LIGHTING = true;
	public static final String NAME = "SteTech1";
	
	// User Data
	public static final String ENTITY = "Entity";
	
	public static void p(String s) {
		System.out.println(System.currentTimeMillis() + ": " + s);
	}

	
/*	public static String getCrateTex() {
		if (NEON) {
			return "Textures/10125-v4.jpg"; //glowingbox.png";
		} else {
			int i = NumberFunctions.rnd(1, 10);
			return "Textures/boxes and crates/" + i + ".png";
		}
	}
	
	
	public static String getRoadwayTex() {
		if (NEON) {
			return "Textures/tron_blue.jpg";
		} else {
			int i = NumberFunctions.rnd(1, 10);
			return "Textures/floor0041.png";
		}
	}
*/
}
