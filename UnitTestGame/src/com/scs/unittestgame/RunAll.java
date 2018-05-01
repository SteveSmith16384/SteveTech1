package com.scs.unittestgame;

public class RunAll {

	public static void main(String[] args) {
		try {
			new UnitTestGameServer();
			
			for (int i=0 ; i<3 ; i++) {
				new UnitTestGameClient();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
