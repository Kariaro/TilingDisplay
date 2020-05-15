package tiling.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import render.gui.GuiFileChooser;

public class TilingUtil {
	/**
	 * 0: Minimal
	 * 1: Partial
	 * 2: Greater
	 * 3: Maximum
	 */
	private static int DEBUG_LEVEL = 0;
	private static boolean DEBUG;
	
	public static void setDebug(boolean enable) {
		DEBUG = enable;
	}
	
	public static boolean isDebug() {
		return DEBUG;
	}
	public static void setDebugLevel(int level) {
		DEBUG_LEVEL = level;
	}
	
	public static int getDebugLevel() {
		return DEBUG_LEVEL;
	}
	
	private static java.io.File defaultPath;
	public static void setDefaultPath(java.io.File file) {
		defaultPath = file;
	}
	public static java.io.File getDefaultPath() {
		return defaultPath;
	}
	
	public static void applyDebugLevel(Logger logger) {
		if(DEBUG) {
			switch(DEBUG_LEVEL) {
				case 0: logger.setLevel(Level.INFO); break;
				case 1: logger.setLevel(Level.FINE); break;
				case 2: logger.setLevel(Level.FINER); break;
				case 3: logger.setLevel(Level.ALL); break;
				default:
			}
		} else {
			logger.setLevel(Level.INFO);
		}
	}
	
	public static boolean checkBlockingInput() {
		return checkBlockingInput(true);
	}
	
	public static boolean checkBlockingInput(boolean beep) {
		if(GuiFileChooser.parentFrame != null) {
			if(GuiFileChooser.parentFrame.isVisible()) {
				/*if(beep) Toolkit.getDefaultToolkit().beep();
				
				for(Window w : JDialog.getWindows()) {
					if(w instanceof JDialog) {
						JDialog dialog = (JDialog)w;
						dialog.setAutoRequestFocus(true);
						
						if(!dialog.isFocused() || !dialog.hasFocus()) {
							dialog.requestFocusInWindow();
							dialog.requestFocus();
							dialog.toFront();
							System.out.println(dialog.isFocused());
						}
					}
				}*/
				
				return true;
			}
		}
		
		return false;
	}
	
	private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
	public static void execute(Runnable task) {
		executorService.execute(task);
	}
	
	public static void executeWithTimeout(Runnable task, int timeout, TimeUnit unit) {
		executorService.execute(() -> {
			Thread thread = new Thread(() -> {
				try {
					task.run();
				} catch(Exception e) {
					e.printStackTrace();
				}
			});
			thread.setDaemon(true);
			thread.start();
			
			try {
				unit.timedJoin(thread, timeout);
				thread.interrupt();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
	
	private static final Queue<Runnable> tasks = new LinkedList<Runnable>();
	public static void invokeLater(Runnable task) {
		synchronized(tasks) {
			tasks.add(task);
		}
	}
	
	
	public static void pollEvents() {
		synchronized(tasks) {
			while(!tasks.isEmpty()) {
				Runnable task = tasks.poll();
				task.run();
			}
		}
	}
	
	public static void stopEvents() {
		executorService.shutdown();
	}
	
	// Debugging
	private static long lastTime = -1;
	public static long time() {
		long oldTime = lastTime;
		lastTime = System.nanoTime();
		
		if(oldTime < 0) return -1;
		return lastTime - oldTime;
	}
	
	public static void printTime(String message) {
		long time = time();
		System.out.printf("%s %.5f ms\n", message, time / 1000000.0);
	}
	
	public static String arrayToString(float[] array) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(float f : array) {
			sb.append(String.format("%.5f", f)).append(", ");
		}
		if(array.length > 0) {
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.append("]").toString();
	}
}
