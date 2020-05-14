package render.main;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.LogManager;

import javax.swing.UIManager;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import tiling.mesh.Texture;
import tiling.parser.TilingDefault;
import tiling.util.FileUtils;
import tiling.util.TilingUtil;

public class Tiling implements Runnable {
	static {
		try {
			// The ConsoleHandler is initialized once inside LogManager.RootLogger
			// if we change Sytem.err to System.out when the ConsoleHandler is created
			// we change it's output stream to System.out.
			
			PrintStream error_stream = System.err;
			System.setErr(System.out);
			
			Locale.setDefault(Locale.ENGLISH);
			LogManager.getLogManager().readConfiguration(new ByteArrayInputStream((
				"handlers=java.util.logging.ConsoleHandler\r\n" + 
				".level=INFO\r\n" + 
				"java.util.logging.ConsoleHandler.level=ALL\r\n" + 
				"java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter\r\n" + 
				"java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] [%3$s] %5$s%n"
			).getBytes()));
			
			// Interact with the RootLogger so that it calls LogManager.initializeGlobalHandlers();
			LogManager.getLogManager().getLogger("").removeHandler(null);
			
			// Switch back to normal error stream
			System.setErr(error_stream);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		if(args.length > 0) {
			if(args[0].equals("-debug") || args[0].equals("-d")) {
				DEBUG = true;
				DEBUG_LEVEL = 3;
				
				System.setProperty("org.lwjgl.util.Debug", "true");
			}
		}
		
		
		try {
			String protocol = Tiling.class.getResource("Tiling.class").getProtocol();
			if(protocol.equals("jar") || protocol.equals("rsrc")) {
				File jar_file = new File(Tiling.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				
				if(System.console() == null) {
					Runtime.getRuntime().exec("cmd.exe /K start cmd.exe /C \"java -jar \"" + jar_file.getAbsolutePath() + "\"\" " + (DEBUG ? "-d &PAUSE":""));
					System.exit(0);
				}
				
				if(!LWJGL_PATH.exists()) LWJGL_PATH.mkdir();
				if(!EXAMPLES_PATH.exists()) EXAMPLES_PATH.mkdir();
				
				System.setProperty("java.library.path", "lwjgl/");
				System.setProperty("org.lwjgl.system.SharedLibraryExtractPath", LWJGL_PATH.getAbsolutePath());
				
				Tiling.customTilingFolder = new File("custom_tilings");
				if(!Tiling.customTilingFolder.exists()) {
					Tiling.customTilingFolder.mkdir();
				}
				
				{
					FileOutputStream stream = new FileOutputStream(new File(EXAMPLES_PATH, "documentation.README"));
					stream.write(FileUtils.readStreamBytes(Tiling.class.getResourceAsStream("/patterns/documentation.README")));
					stream.close();
					
					for(String name : TilingDefault.DEFAULT_TILINGS) {
						String rest = name.substring(name.lastIndexOf('/') + 1);
						rest = rest.substring(0, rest.indexOf('.'));
						
						stream = new FileOutputStream(new File(EXAMPLES_PATH, rest + ".example"));
						stream.write(FileUtils.readStreamBytes(Tiling.class.getResourceAsStream(name)));
						stream.close();
					}
				}
			} else {
				customTilingFolder = new File(Tiling.class.getResource("/patterns/").toURI());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Tiling tiling = new Tiling();
		tiling.start();
	}
	
	public static final BufferedImage ICON = Texture.loadLocalImage("/icons/Icon3_48x48.png");
	public static final File EXAMPLES_PATH = new File("examples");
	public static final File LWJGL_PATH = new File("lwjgl");
	public static final String AUTHOR = "Victor";
	public static final String VERSION = "1.0.1";
	
	public static File customTilingFolder;
	
	public static boolean DEBUG = false;
	public static int DEBUG_LEVEL = 0;
	

	public static final int TARGET_FPS = 120;
	public static int fps = 0;
	
	private int height = (int)(540 * 1.5);
	private int width = (int)(960);
	
	private TilingRender render;
	private boolean running;
	private Thread thread;
	private long window;
	
	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Main Thread");
		thread.start();
	}
	
	public synchronized void stop() {
		if(!running) return;
		// TODO: Implement if needed
	}
	
	private void init() throws Exception {
		if(!glfwInit()) {
			return;
		}
		
		// glfwWindowHint(GLFW_SAMPLES, 4);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		window = glfwCreateWindow(width, height, "Tiling (Press 'U' Zoom) (Press 'I' UnZoom) (Press 'M' Menu)", NULL, NULL);
		if(window == NULL) {
			return;
		}
		
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
		
		glfwSetKeyCallback(window, new Input());
		glfwSetCursorPosCallback(window, new Mouse(0));
		glfwSetMouseButtonCallback(window, new Mouse(1));
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				Tiling.this.width = width;
				Tiling.this.height = height;
				
				render.setViewport(width, height);
			}
		});
		
		glfwMakeContextCurrent(window);
		if(ICON != null) {
			GLFWImage image = GLFWImage.malloc();
			GLFWImage.Buffer buffer = GLFWImage.malloc(1);
			image.set(ICON.getWidth(), ICON.getHeight(), Texture.loadBuffer(ICON));
			buffer.put(0, image);
			glfwSetWindowIcon(window, buffer);
		}
		
		GL.createCapabilities();
		
		render = new TilingRender(this, window);
		glfwShowWindow(window);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void run() {
		try {
			init();
			
			render.render();
		} catch(Exception e) {
			e.printStackTrace();
			
			System.exit(0);
		}
		
		double SLEEP_TIME = 1000.0 / (double)TARGET_FPS;
		
		int frames = 0;
		long last = System.currentTimeMillis();
		double next = System.currentTimeMillis() + SLEEP_TIME;
		while(running) {
			try {
				long aaaa = System.currentTimeMillis();
				if(aaaa < next) {
					Thread.sleep((long)(next - aaaa));
				}
				next += SLEEP_TIME;
				if(aaaa > next + SLEEP_TIME) {
					next += (long)((aaaa - next) / SLEEP_TIME) * SLEEP_TIME;
					// Target fps not reached!
					// Fps is lower than TARGET_FPS
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			try {
				//long now = System.nanoTime();
				render.render();
				render.update();
				//long ellipsed = System.nanoTime() - now;
				//delta_time = ellipsed / 1000000.0f;
				// System.out.println("test: " + delta_time);
				frames++;
			} catch(Exception e) {
				System.out.println("[FATAL EXCEPTION PREVENTED]");
				e.printStackTrace();
			}
			
			long now = System.currentTimeMillis();
			if(now - last > 1000) {
				//System.out.println("fps: " + frames + "/" + TARGET_FPS);
				Tiling.fps = frames;
				
				frames = 0;
				last += 1000;
			}
			
			if(glfwWindowShouldClose(window)) {
				running = false;
			}
		}
		
		// TODO: What to do here?
		glfwTerminate();
		TilingUtil.stopEvents();
		
		// TODO: Remove
		System.exit(0);
	}
}
