package ssmith.lang;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Functions {

	public static void sleep(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public static String Exception2String(Throwable ex) {
		StringBuffer str = new StringBuffer();
		while (ex != null) {
			str.append(ex + "\n");
			for (int c = 0; c < ex.getStackTrace().length; c++) {
				str.append(" " + ex.getStackTrace()[c].getClassName());
				str.append(":" + ex.getStackTrace()[c].getLineNumber() + " - ");
				str.append(ex.getStackTrace()[c].getMethodName());
				str.append("\n");
			}
			ex = ex.getCause();
			if (ex != null) {
				str.append("Caused by:\n");
			}
		}
		return str.toString();
	}


	public static String readAllTextFileFromJar(String filename) throws IOException {
		StringBuilder result = new StringBuilder("");

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(filename);
		InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
		BufferedReader in = new BufferedReader(streamReader);

		for (String line; (line = in.readLine()) != null;) {
			result.append(line).append("\n");
		}

		return result.toString();
	}


	public static byte[] readAllBinaryFileFromJar(String filename) throws IOException {
		//InputStream inputStream = ClassLoader.getSystemClassLoader().getSystemResourceAsStream(filename);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(filename);

		BufferedInputStream is = new BufferedInputStream(inputStream);
		
		byte[] b = new byte[is.available()];
		is.read(b);
		return b;
	}


}
