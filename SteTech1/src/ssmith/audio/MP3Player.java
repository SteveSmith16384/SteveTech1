package ssmith.audio;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class MP3Player extends Thread {

	private String mp3_filename;
	private volatile boolean stop_now = false;
	public volatile boolean paused = false;
	private boolean loop;

	public MP3Player(String fname, boolean _loop) {
		super("MP3Player");

		loop = _loop;
		this.mp3_filename = fname;

		this.setDaemon(true);
	}


	public void run() {
		AudioInputStream din = null;
		try {
			do {
				ClassLoader cl = this.getClass().getClassLoader();
				InputStream is = cl.getResourceAsStream(mp3_filename);
				AudioInputStream in = AudioSystem.getAudioInputStream(is);
				AudioFormat baseFormat = in.getFormat();
				AudioFormat decodedFormat = new AudioFormat(
						AudioFormat.Encoding.PCM_SIGNED,
						baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
						baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
						false);
				din = AudioSystem.getAudioInputStream(decodedFormat, in);
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
				SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
				if (line != null) {
					line.open(decodedFormat);
					byte[] data = new byte[4096];
					// Start
					line.start();

					int nBytesRead;
					while ((nBytesRead = din.read(data, 0, data.length)) != -1 && stop_now == false) {
						line.write(data, 0, nBytesRead);
						while (this.paused && stop_now == false) {
							Thread.sleep(200);
						}
					}
					// Stop
					line.drain();
					line.stop();
					line.close();
					din.close();
					in.close();
				} else {
					throw new IOException("File '" + this.mp3_filename + "' does not exist");
				}
			} while (loop);
		} catch(Exception ex) {
			System.err.println("Cannot play '" + this.mp3_filename + "': " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			if(din != null) {
				try { 
					din.close(); 
				} catch(IOException e) { 
					// Do nothing
				}
			}
		}
	}


	public void stopNow() {
		this.stop_now = true;
	}

}