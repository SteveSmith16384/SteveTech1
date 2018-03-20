package com.scs.stevetech1.server;

import java.util.Random;

import ssmith.io.IOFunctions;
import ssmith.lang.Functions;


public class Globals {
	
	public static final boolean RECORD_VID = false;
	public static final boolean STRICT = true; // Extra checks (which might slow stuff down)

	// Lots of consts for specific debugging output
	public static final boolean PROFILE_SERVER = true;
	public static final boolean SHOW_NUM_ENT_UPDATES_SENT = true;
	public static final boolean SHOW_SERVER_AVATAR_ON_CLIENT = true;

	public static final boolean DEBUG_CAN_SEE = false;
	public static final boolean DEBUG_SLIDING_DOORS = false;
	
	public static final boolean SIMULATE_DROPPED_PACKETS = false;
	public static final int PCENT_DROPPED_PACKETS = 0;
	public static final int MIN_ARTIFICIAL_COMMS_DELAY = 0;
	public static final int MAX_ARTIFICIAL_COMMS_DELAY = 0;
	
	public static final boolean FEW_MODELS = false;
	public static final boolean DEBUG_ENTITY_ADD_REMOVE = false;
	public static final boolean EMPTY_MAP = false;
	public static final boolean DEBUG_SERVER_SHOOTING = false;
	public static final boolean PLAYERS_START_IN_CORNER = false;
	public static final boolean DEBUG_CLIENT_ROTATION = false;
	public static final boolean USE_SERVER_MODELS_ON_CLIENT = false;
	public static final boolean DEBUG_PLAYER_RESTART = false;
	public static final boolean DEBUG_MOUNTAIN_BORDER = false;
	public static final boolean SHOW_SNOWBALL_COLLISION_POS = false;
	public static final boolean STOP_SERVER_AVATAR_MOVING = false;
	public static final boolean DEBUG_ADJ_AVATAR_POS = false;
	public static final boolean SHOW_LATEST_AVATAR_POS_DATA_TIMESTAMP = false;
	public static final boolean SHOW_AVATAR_WALK_DIR = false;
	public static final boolean DEBUG_REWIND_POS1 = false;
	public static final boolean DEBUG_HUD = false;
	public static final boolean LOG_MOVING_TARGET_POS = false;
	public static final boolean DEBUG_MSGS = false;
	public static final boolean DEBUG_SHOOTING_POS = false;

	public static final int PING_INTERVAL_MS = 5 * 1000; // How often server sends pings
	public static final boolean ONLY_ADJUST_CLIENT_ON_MOVE = false; // Only adjust the client avatar's position when the player moves them.

	public static final int KRYO_WRITE_BUFFER_SIZE = 16384*6;
	public static final int KRYO_OBJECT_BUFFER_SIZE = 2048*6;
	// Our movement speed
	public static final float SMALLEST_MOVE_DIST = 0.001f;// 0.02f;
	public static final float MAX_MOVE_DIST = 4f;

	public static final float CAM_DIST = 50f;
	public static final boolean LIGHTING = true;

	// User Data
	public static final String ENTITY = "Entity";

	public static final Random rnd = new Random();

	private static final long GAME_START_TIME = System.currentTimeMillis();
	

	public static void p(String s) {
		System.out.println(System.currentTimeMillis() + ": " + s);
	}


	public static void pe(String s) {
		System.err.println(System.currentTimeMillis() + ": " + s);
	}

	
	public static void HandleError(Exception ex) {
		ex.printStackTrace();
		
		String filename = "Errors_" + GAME_START_TIME + ".log";
		IOFunctions.appendToFile(filename, Functions.Exception2String(ex));
		
	}
	
}
