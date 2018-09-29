package com.scs.stevetech1.client;

import com.jme3.math.ColorRGBA;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractSimpleGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

public abstract class AbstractSimpleGameClient extends AbstractGameClient {
	
	private AbstractCollisionValidator collisionValidator;
	private String ipAddress;
	private int port;
	private String playerName;

	public AbstractSimpleGameClient(String title, String serverIp, int gamePort, String _playerName) {
		super(new ValidateClientSettings(AbstractSimpleGameServer.GAME_CODE, AbstractSimpleGameServer.VERSION, AbstractSimpleGameServer.KEY), title, null, 
				Globals.DEFAULT_TICKRATE, Globals.DEFAULT_RENDER_DELAY, Globals.DEFAULT_NETWORK_TIMEOUT, 1f);
		
		ipAddress = serverIp;
		port = gamePort;
		playerName = _playerName;
	}


	@Override
	protected String getPlayerName() {
		return playerName.length() > 0 ? this.playerName : super.getPlayerName();
	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		this.getViewPort().setBackgroundColor(ColorRGBA.Black);

		collisionValidator = new AbstractCollisionValidator();

		this.connect(ipAddress, port, false);
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return collisionValidator.canCollide(a, b);
	}
	
	
	@Override
	protected Class[] getListofMessageClasses() {
		// TODO Auto-generated method stub
		return null;
	}


}
