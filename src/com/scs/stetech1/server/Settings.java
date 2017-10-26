package com.scs.stetech1.server;

import com.jme3.network.serializing.Serializer;
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
	public static final int PORT = 6143;
	//public static final int SEND_INPUT_INTERVAL_MS = 50;
	public static final int SERVER_TICKRATE_MS = 20;
	public static final int SERVER_SEND_UPDATE_INTERVAL_MS = 1000; // todo - make 50/100  // scs check
	public static final int CLIENT_RENDER_DELAY = 100;
	public static final int PING_INTERVAL_MS = 10 * 1000;
	
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



	}
	
}
