package ssmith.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class LogWindow extends JFrame {

	private int width, height;

	private JTextArea textArea = null;
	private JScrollPane pane = null;
	private String text = "";

	public LogWindow(String title, int width, int height) {
		super(title);
		setSize(width, height);
		textArea = new JTextArea();
		pane = new JScrollPane(textArea);
		getContentPane().add(pane);
		setVisible(true);

		//textArea.setDoubleBuffered(true);

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
					Thread.sleep(500);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							textArea.setText(text);
						}
					});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.setDaemon(true);
		t.start();

	}


	/**
	 * This method appends the data to the text area.
	 * 
	 * @param data
	 *            the Logging information data
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	/*public void appendText(final String data) {
		//textArea.append(data);
		//this.getContentPane().validate();
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					textArea.append(data);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/


	public void setText(final String data) {
		//textArea.setText(data);
		//this.getContentPane().validate();
		/*try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					textArea.setText(data); // data.toString()
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		//if (text != null) {
		//synchronized (text) {
			text = data;
		//}
		//}

	}
}

