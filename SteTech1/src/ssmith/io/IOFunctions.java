package ssmith.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class IOFunctions {

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
