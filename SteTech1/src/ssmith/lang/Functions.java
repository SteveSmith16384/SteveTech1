package ssmith.lang;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

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


	public static String readAllFileFromJar(ClassLoader classLoader, String filename) throws FileNotFoundException {
		StringBuilder result = new StringBuilder("");

		//Get file from resources folder
		//ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(filename).getFile());

		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}
			scanner.close();
		}

		return result.toString();
	}


}
