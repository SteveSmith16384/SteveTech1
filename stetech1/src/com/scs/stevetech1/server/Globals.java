package com.scs.stevetech1.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


public class Globals {
	
	public static final boolean RECORD_VID = false;
	public static final boolean LIVE_SERVER = false; // Adjusts network idle thresholds etc...

	public static final boolean DEBUG_SERVER_SHOOTING = true;
	public static final boolean DEBUG_PLAYER_RESTART = true;
	public static final boolean PLAYERS_START_IN_CORNER = true;
	public static final boolean EMPTY_MAP = true;
	
	public static final boolean DEBUG_MOUNTAIN_BORDER = false;
	public static final boolean DEBUG_ENTITY_ADD_REMOVE = false;	
	public static final boolean SHOW_SNOWBALL_COLLISION_POS = false;
	public static final boolean STOP_SERVER_AVATAR_MOVING = false;
	public static final boolean SHOW_SERVER_AVATAR_ON_CLIENT = false;
	public static final boolean DEBUG_ADJ_AVATAR_POS = false;
	public static final boolean USE_SERVER_MODELS_ON_CLIENT = false;
	public static final boolean SHOW_LATEST_AVATAR_POS_DATA_TIMESTAMP = false;
	public static final boolean SHOW_AVATAR_WALK_DIR = false;
	public static final boolean DEBUG_REWIND_POS1 = false;
	public static final boolean DEBUG_HUD = false;
	public static final boolean LOG_MOVING_TARGET_POS = false;
	public static final boolean DEBUG_MSGS = false;
	public static final boolean DEBUG_SHOOTING_POS = false;

	public static final float CONNECTION_IDLE_THRESH = 1000000;

	public static final int SERVER_TICKRATE_MS = 25; // Source: 15ms
	public static final int SERVER_SEND_UPDATE_INTERVAL_MS = 40; // How often server sends entity updates.  This must be fast enough so the client has recent data to work with 
	public static final int CLIENT_RENDER_DELAY = 200; //SERVER_SEND_UPDATE_INTERVAL_MS*3; // How far in past the client should render the view.  Source: 50ms
	public static final int PING_INTERVAL_MS = 5 * 1000; // How often server sends pings
	public static final int ARTIFICIAL_COMMS_DELAY = 0;

	public static final boolean SHOW_LOGO = false;

	// Our movement speed
	public static final float PLAYER_MOVE_SPEED = 3f;
	public static final float JUMP_FORCE = 2f;
	public static final float SMALLEST_MOVE_DIST = 0.02f;

	public static final float CAM_DIST = 50f;
	public static final boolean LIGHTING = true;
	public static final String NAME = "SteTech1";

	// Changing settings
	public static boolean SYNC_AVATAR_POS = true;
	public static boolean SYNC_GRENADE_POS = true;

	// User Data
	public static final String ENTITY = "Entity";

	public static final Random rnd = new Random();

	static {
		if (ARTIFICIAL_COMMS_DELAY + SERVER_SEND_UPDATE_INTERVAL_MS >= CLIENT_RENDER_DELAY) {
			throw new RuntimeException("Data will not be sent in time for the client to use it to render");
		}
	}


	public static void p(String s) {
		System.out.println(System.currentTimeMillis() + ": " + s);
	}


	public static void pe(String s) {
		System.err.println(System.currentTimeMillis() + ": " + s);
	}


	public static void appendToFile(String path, String text) {
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(path, true));
			bw.write(text);
			bw.newLine();
			bw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally { // always close the file
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException ioe2) {
					// just ignore it
				}
			}

		}
	}

}