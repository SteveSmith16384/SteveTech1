package com.scs.stetech1.shared;

public class EntityTypes {

	public static final int AVATAR = 1;
	public static final int CRATE = 2;
	public static final int FLOOR = 3;
	public static final int FENCE = 4;
	public static final int WALL = 5;
	public static final int DEBUGGING_SPHERE = 6;
	public static final int MOVING_TARGET = 7;
	public static final int LASER_BULLET = 8;
	public static final int GRENADE = 9;
	
	private EntityTypes() {
	}
	
	
	public static String getName(int type) {
		switch (type) {
		case AVATAR: return "Avatar";
		case CRATE: return "CRATE";
		case FLOOR: return "FLOOR";
		case FENCE: return "FENCE";
		case WALL: return "WALL";
		case DEBUGGING_SPHERE: return "DEBUGGING_SPHERE";
		case MOVING_TARGET: return "MOVING_TARGET";
		case LASER_BULLET: return "LASER_BULLET";
		case GRENADE: return "GRENADE";
		default: return "UNKNOWN (" + type + ")";
		}
	}

}
