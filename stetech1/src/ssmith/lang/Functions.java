package ssmith.lang;

public class Functions {

	
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



}
