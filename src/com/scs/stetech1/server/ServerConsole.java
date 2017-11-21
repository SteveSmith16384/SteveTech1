package com.scs.stetech1.server;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import ssmith.swing.Console;

public class ServerConsole extends Console implements IConsole, KeyListener {

	private AbstractGameServer server;

	public ServerConsole(AbstractGameServer _server) {
		server = _server;

		super.input.addKeyListener(this);
	}


	@Override
	public void appendText(String text) {
		super.appendText(text);

	}


	@Override
	public void keyReleased(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
			//this.textArea.append("ENTER Pressed!\n");
			server.handleCommand(this.input.getText().trim());
			this.input.setText("");
		}

	}



}
