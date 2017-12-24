package com.scs.stevetech1.lobby;

import java.io.IOException;

import com.scs.stevetech1.networking.IMessageClient;
import com.scs.stevetech1.networking.KryonetClient;

public class RequestServerDataThread implements Runnable {

	private LobbyMain main;
	private IMessageClient networkClient;

	public RequestServerDataThread(LobbyMain _main) {
		main = _main;
	}

	@Override
	public void run() {
		try {
			networkClient = new KryonetClient(main);// SpiderMonkeyClient(this);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		
	}

}
