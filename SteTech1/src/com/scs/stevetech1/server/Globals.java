package com.scs.stevetech1.server;

import java.util.Random;

import ssmith.io.IOFunctions;
import ssmith.lang.Functions;


public class Globals {
	
	public static final boolean RECORD_VID = false;
	public static final boolean RELEASE_MODE = false; // Turn off simulated packet dropping etc...
	public static final boolean STRICT = true; // Extra checks (which will slow the game down)

	public static final boolean SLEEP_BETWEEN_NEW_ENT_MSGS = true;
	public static final boolean DEBUG_RELOAD_PROBLEM = true;

	// Lots of consts for specific debugging output
	public static final boolean SHOW_BULLET_COLLISION_POS = false;
	public static final boolean SHOW_BULLET_POSITIONS = false;
	public static final boolean FORCE_CLIENT_SLOWDOWN = false;
	public static final boolean TEST_BULLET_REWINDING = false;
	public static final boolean EMPTY_MAP = false;
	public static final boolean SHOW_FPS_STATS = false;
	public static final boolean SHOW_SERVER_AVATAR_ON_CLIENT = false;
	public static final boolean DEBUG_GUN_NOT_SHOWING = false;
	public static final boolean NO_AI_UNITS = false;
	public static final boolean TEST_OUTLINE_SHADER = false;
	public static final boolean DEBUG_MSGS = false;
	public static final boolean DEBUG_ENTITY_ADD_REMOVE = false;
	public static final boolean DEBUG_DIE_ANIM = false;
	public static final boolean PLAYERS_START_IN_CORNER = false;
	public static final boolean DEBUG_PLAYER_START_POS = false;
	public static final boolean DEBUG_GUN_ROTATION = false;	
	public static final boolean DEBUG_GAME_STATUS_CHECK = false;
	public static final boolean FEW_MODELS = false;
	public static final boolean DEBUG_NO_UPDATE_MSGS = false;
	public static final boolean AI_IGNORE_PLAYER = false;
	public static final boolean TRANSPARENT_WALLS = false;
	public static final boolean DEBUG_GAME_NOT_STARTING = false;
	public static final boolean TEST_AI = false;
	public static final boolean DEBUG_DELAYED_EXPLOSION = false;
	public static final boolean DEBUG_AI_BULLET_POS = false;
	public static final boolean DEBUG_BULLET_HIT = false;
	public static final boolean DEBUG_AI_TARGETTING = false;
	public static final boolean DEBUG_HUD = false;
	public static final boolean DEBUG_AVATAR_SET = false;
	public static final boolean DEBUG_SLIDING_DOORS = false;
	public static final boolean DEBUG_CLIENT_SERVER_FAR_APART = false;
	public static final boolean USE_BOXES_FOR_AVATARS_SOLDIER = false;
	public static final boolean SHOW_IF_SYSTEM_TOO_SLOW = false;
	public static final boolean TURN_OFF_CLIENT_POS_ADJ = false;
	public static final boolean SHOW_SERVER_CLIENT_AVATAR_DIST = false;
	public static final boolean SHOW_NUM_ENT_UPDATES_SENT = false;
	public static final boolean DEBUG_PLAYER_RESTART = false;
	public static final boolean STOP_SERVER_AVATAR_MOVING = false;
	public static final boolean DEBUG_ADJ_AVATAR_POS = false;
	public static final boolean SHOW_LATEST_AVATAR_POS_DATA_TIMESTAMP = false;
	public static final boolean DEBUG_REWIND_POS1 = false;
	public static final boolean LOG_MOVING_TARGET_POS = false;
	public static final boolean DEBUG_SHOOTING_POS = false;

	// Generic entity codes - all should be negative to avoid collision with custom entity types, which should all be positive.
	public static final int DEBUGGING_SPHERE = -1;
	public static final int BULLET_TRAIL = -2;
	public static final int EXPLOSION_SHARD = -3;
	public static final int EXPLOSION_SPHERE = -4;

	// Effects
	//public static final boolean TOONISH = false; // Outlines
	public static final boolean BULLETS_CONES = true;
	public static final boolean SHOW_VIEW_FROM_KILLER_ON_DEATH = false;

	//----
	public static final int PCENT_DROPPED_PACKETS = 10;
	public static final int MIN_ARTIFICIAL_COMMS_DELAY = 0;//50; //0;
	public static final int MAX_ARTIFICIAL_COMMS_DELAY = 0;//100;
	//----
	
	public static final int DEFAULT_TICKRATE = 25;
	public static final int DEFAULT_SEND_UPDATES_INTERVAL = 40;
	public static final int DEFAULT_RENDER_DELAY = 200;
	public static final int DEFAULT_NETWORK_TIMEOUT = 100000;
	
	public static final int PING_INTERVAL_MS = 5 * 1000; // How often server sends pings
	public static final int SUBNODE_SIZE = 15;
	public static final int DEFAULT_VOLUME = 1;
	public static final long HISTORY_DURATION_MILLIS = 5000; // todo - make same as avatar restart time

	public static final int KRYO_WRITE_BUFFER_SIZE = 16384*7;
	public static final int KRYO_OBJECT_BUFFER_SIZE = 4096*3*24;
	
	// Our movement speed
	public static final float SMALLEST_MOVE_DIST = 0.001f;
	public static final float MAX_MOVE_DIST = 2f; // was 1f

	public static final float CAM_VIEW_DIST = 150f;

	// User Data
	public static final String ENTITY = "Entity";

	public static final Random rnd = new Random();

	public static void showWarnings() {
		if (RELEASE_MODE == false) {
			p("WARNING: Game NOT in release mode");
		}
	}
	
	
	public static void p(String s) {
		System.out.println(System.currentTimeMillis() + ": " + s);
	}


	public static void pe(String s) {
		System.err.println(System.currentTimeMillis() + ": " + s);
	}

	
	public static void HandleError(Exception ex) {
		ex.printStackTrace();
		
		String filename = "Errors_" + System.currentTimeMillis() + ".log";
		IOFunctions.appendToFile(filename, Functions.Exception2String(ex));
		
	}
	
}
