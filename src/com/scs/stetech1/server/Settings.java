package com.scs.stetech1.server;

import java.util.Random;

import com.jme3.network.serializing.Serializer;
import com.scs.stetech1.netmessages.AllEntitiesSentMessage;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerAckMessage;
import com.scs.stetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.netmessages.PlayerInputMessage;
import com.scs.stetech1.netmessages.PlayerLeftMessage;
import com.scs.stetech1.netmessages.RemoveEntityMessage;
import com.scs.stetech1.netmessages.UnknownEntityMessage;


public class Settings {

	public static final boolean DEBUG = true;
	//public static final boolean ALWAYS_UPDATE_ALL_ENTITIES = true;
	public static final boolean VERBOSE = true;

	public static final Random rnd = new Random();

	public static final int PORT = 6143;
	public static final int SERVER_TICKRATE_MS = 20; // Source: 15ms
	public static final int SERVER_SEND_UPDATE_INTERVAL_MS = 100; // This must be fast enough so the client has recent data to work with 
	public static final int CLIENT_RENDER_DELAY = SERVER_SEND_UPDATE_INTERVAL_MS*2; // Source: 50ms
	public static final int PING_INTERVAL_MS = 10 * 1000;
	public static final int COMMS_DELAY = 50;

	public static final String VERSION = "0.01";
	public static final boolean SHOW_LOGO = false;
	public static final boolean RECORD_VID = false;
	public static final boolean USE_MODEL_FOR_PLAYERS = false;

	// DEBUG
	//public static final boolean DEBUG_HUD = false;

	// Our movement speed
	public static final float PLAYER_MOVE_SPEED = 3f;
	public static final float JUMP_FORCE = 8f;

	public static final float CAM_DIST = 50f;
	public static final boolean LIGHTING = true;
	public static final String NAME = "SteTech1";

	// User Data
	public static final String ENTITY = "Entity";

	public static void p(String s) {
		System.out.println(System.currentTimeMillis() + ": " + s);
	}


	public static void Register() {
		Serializer.registerClass(PingMessage.class);
		Serializer.registerClass(NewPlayerRequestMessage.class);
		Serializer.registerClass(NewPlayerAckMessage.class);
		Serializer.registerClass(PlayerInputMessage.class);
		Serializer.registerClass(UnknownEntityMessage.class);
		Serializer.registerClass(NewEntityMessage.class);
		Serializer.registerClass(EntityUpdateMessage.class);
		Serializer.registerClass(PlayerLeftMessage.class);
		Serializer.registerClass(RemoveEntityMessage.class);
		Serializer.registerClass(AllEntitiesSentMessage.class);

		// If you add any, don't forget to add the listener to the client or server!! 

	}

}
