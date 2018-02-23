package ssmith.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TextConsole extends Thread {

	private ConsoleInputListener listener;
	
	public TextConsole(ConsoleInputListener _listener) {
		super(TextConsole.class.getSimpleName());
		
		listener = _listener;
		start();
	}


	public void run() {
		while (true) {
			try {
				System.out.println("> ");
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String line = reader.readLine();
				this.listener.processConsoleInput(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
