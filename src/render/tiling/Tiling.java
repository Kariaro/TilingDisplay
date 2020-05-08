package render.tiling;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.LogManager;

import javax.swing.UIManager;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import render.generate.TilingDefault;
import render.generate.TilingPattern;
import render.gui.Gui;
import render.main.Camera;
import render.main.FileUtils;
import render.main.Input;
import render.main.Mouse;
import render.math.MathUtils;
import render.mesh.TilingMesh;

public class Tiling implements Runnable {
	static {
		try {
			Locale.setDefault(Locale.ENGLISH);
			LogManager.getLogManager().readConfiguration(new ByteArrayInputStream((
				"handlers=java.util.logging.ConsoleHandler\r\n" + 
				".level=INFO\r\n" + 
				"java.util.logging.ConsoleHandler.level=ALL\r\n" + 
				"java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter\r\n" + 
				"java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] [%3$s] %5$s%n"
			).getBytes()));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final File EXAMPLES_PATH = new File("examples");
	public static final File LWJGL_PATH = new File("lwjgl");
	public static File customTilingFolder;
	
	public static void main(String[] args) {
		if(args.length > 0) {
			if(args[0].equals("-debug") || args[0].equals("-d")) {
				DEBUG = true;
			}
		}
		
		System.setProperty("org.lwjgl.util.Debug", "true");
		
		try {
			String text = Tiling.class.getResource("Tiling.class").toString();
			boolean insideJar = text.startsWith("jar:") || text.startsWith("rsrc:");
			
			if(insideJar) {
				File jar_file = new File(Tiling.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				
				if(System.console() == null) {
					Runtime.getRuntime().exec("cmd.exe /K start cmd.exe /C \"java -jar \"" + jar_file.getAbsolutePath() + "\"\"");
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
					stream.write(FileUtils.readStreamBytes(Tiling.class.getResourceAsStream("/patterns/Template.tiling")));
					stream.close();
					
					stream = new FileOutputStream(new File(EXAMPLES_PATH, "Ammann-Beenker.tiling"));
					stream.write(FileUtils.readStreamBytes(Tiling.class.getResourceAsStream("/patterns/Preset_Ammann_Beenker.tiling")));
					stream.close();
					
					stream = new FileOutputStream(new File(EXAMPLES_PATH, "Penrose.tiling"));
					stream.write(FileUtils.readStreamBytes(Tiling.class.getResourceAsStream("/patterns/Preset_Penrose.tiling")));
					stream.close();
				}
			} else {
				customTilingFolder = new File("C:/Users/Admin/Desktop/SSM/GustavTiling/res/patterns/");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		new Tiling().start();
	}
	
	public static boolean DEBUG = false;
	
	private int width = (int)(960 * 1.5);
	private int height = (int)(540 * 1.5);
	
	private boolean running;
	private Thread thread;
	private long window;
	
	private TilingShader shader;
	private Camera camera;
	private Gui gui;
	private TilingMesh background;
	public List<TilingPattern> patterns;
	public TilingPattern customTiling;
	
	public void start() {
		running = true;
		thread = new Thread(this, "Gustav Tiling");
		thread.start();
	}
	
	private void init() throws Exception {
		if(!glfwInit()) {
			return;
		}
		
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
		
		glfwMakeContextCurrent(window);
		glfwShowWindow(window);
		
		GL.createCapabilities();
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		
		camera = new Camera(window);
		camera.z = 1;
		
		gui = new Gui(this, window);
		
		GL11.glDisable(GL_CULL_FACE);
		GL11.glEnable(GL_DEPTH_TEST);
		
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_PROJECTION_MATRIX);
		GL11.glOrtho(0, width, height, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW_MATRIX);
		
		createBackground();
		
		// https://www.desmos.com/calculator/kf3xpyprsf
		patterns = TilingDefault.loadPatterns();
		
		glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height){
				Tiling.this.width = width;
				Tiling.this.height = height;
				Tiling.this.gui.width = width;
				Tiling.this.gui.height = height;
				GL11.glViewport(0, 0, width, height);
				GL11.glLoadIdentity();
				GL11.glMatrixMode(GL11.GL_PROJECTION_MATRIX);
				GL11.glOrtho(0, width, height, 0, 1, -1);
				GL11.glMatrixMode(GL11.GL_MODELVIEW_MATRIX);
			}
		});
	}
	
	private void createBackground() throws Exception {
		shader = new TilingShader();
		shader.createShaderCode(FileUtils.readStream(Tiling.class.getResourceAsStream("/tiling_fragment.fs")), GL20.GL_FRAGMENT_SHADER);
		shader.createShaderCode(FileUtils.readStream(Tiling.class.getResourceAsStream("/tiling_vertex.vs")), GL20.GL_VERTEX_SHADER);
		shader.bindAttrib(0, "in_Position");
		shader.bindAttrib(1, "in_Uv");
		shader.bindAttrib(2, "in_Color");
		shader.link();
		shader.createUniform("projectionView");
		shader.createUniform("transformationMatrix");
		
		background = new TilingMesh("background_mesh");
		Random random = new Random(0);
		
		List<Vector4f> colors = new ArrayList<Vector4f>();
		List<Vector3f> vertices = new ArrayList<Vector3f>();
		
		for(int i = 0; i < 10 * 4 * 4; i++) {
			Vector4f color = new Vector4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1);
			colors.add(color);
			colors.add(color);
			colors.add(color);
		}
		
		for(int i = 0; i < 10 * 3 * 2; i++) {
			Vector4f color = new Vector4f(random.nextFloat() / 1.5f, random.nextFloat() / 1.5f, random.nextFloat() / 1.5f, 1);
			colors.add(color);
			colors.add(color);
			colors.add(color);
		}
		
		float o = -1.5f;
		for(int i = -5; i < 5; i++) {
			for(int j = -3; j < 1; j++) {
				vertices.add(new Vector3f(i + 1,  o, j + 1).mul(5));
				vertices.add(new Vector3f(i + 1,  o, j    ).mul(5));
				vertices.add(new Vector3f(i    ,  o, j    ).mul(5));
				vertices.add(new Vector3f(i    ,  o, j + 1).mul(5));
				vertices.add(new Vector3f(i + 1,  o, j + 1).mul(5));
				vertices.add(new Vector3f(i    ,  o, j    ).mul(5));
				vertices.add(new Vector3f(i    , -o, j    ).mul(5));
				vertices.add(new Vector3f(i + 1, -o, j    ).mul(5));
				vertices.add(new Vector3f(i + 1, -o, j + 1).mul(5));
				vertices.add(new Vector3f(i    , -o, j    ).mul(5));
				vertices.add(new Vector3f(i + 1, -o, j + 1).mul(5));
				vertices.add(new Vector3f(i    , -o, j + 1).mul(5));
			}
		}
		
		float g = -3f;
		for(int i = -5; i < 5; i++) {
			for(int k = -1; k < 2; k++) {
				vertices.add(new Vector3f(i    , k - 0.5f, g).mul(5));
				vertices.add(new Vector3f(i + 1, k - 0.5f, g).mul(5));
				vertices.add(new Vector3f(i + 1, k + 0.5f, g).mul(5));
				vertices.add(new Vector3f(i    , k - 0.5f, g).mul(5));
				vertices.add(new Vector3f(i + 1, k + 0.5f, g).mul(5));
				vertices.add(new Vector3f(i    , k + 0.5f, g).mul(5));
			}
		}
		
		float[] v = new float[vertices.size() * 3];
		float[] c = new float[colors.size() * 4];
		float[] u = new float[colors.size() * 2];
		for(int i = 0; i < v.length / 3; i++) {
			Vector3f vert = vertices.get(i);
			v[i * 3    ] = vert.x;
			v[i * 3 + 1] = vert.y;
			v[i * 3 + 2] = vert.z;
			
			Vector4f cols = colors.get(i);
			c[i * 4    ] = cols.x;
			c[i * 4 + 1] = cols.y;
			c[i * 4 + 2] = cols.z;
			c[i * 4 + 3] = cols.w;
			
			u[i * 2    ] = 0;
			u[i * 2 + 1] = 1;
		}
		
		background.buildObject(v, u, c);
	}
	
	
	public int TARGET_FPS = 120;
	public int fps = 0;
	public void run() {
		try {
			init();
		} catch(Exception e) {
			e.printStackTrace();
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
			
			update();
			render();
			frames++;
			
			
			long now = System.currentTimeMillis();
			if(now - last > 1000) {
				// System.out.println("fps: " + frames + "/" + TARGET_FPS);
				this.fps = frames;
				
				frames = 0;
				last += 1000;
			}
			
			if(glfwWindowShouldClose(window)) {
				running = false;
			}
		}
	}
	
	private void update() {
		glfwSwapBuffers(window);
		glfwPollEvents();
		camera.speed_mod = (float)Math.abs(camera.z) * 10;
		camera.update();
	}
	
	private boolean pattern_changed = true;
	private int pattern_index = 0;
	public void loadPattern(int index) {
		if(pattern_index != index) {
			pattern_changed = true;
			pattern_index = index;
		}
	}
	
	public int getZoom() {
		return zoom_level;
	}
	
	public int getPatternIndex() {
		return pattern_index;
	}
	
	public void setPatternIndex(int index) {
		pattern_changed = true;
		pattern_index = index;
	}
	
	private boolean pressing;
	private boolean pressed;
	private int zoom_level = 0;
	private void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		if(Input.keys[GLFW_KEY_9]) {
			System.out.println(Long.toHexString(window));
			glfwWindowHint(GLFW_FOCUSED, GLFW_FALSE);
			java.awt.Toolkit.getDefaultToolkit().beep();
		}
		
		camera.rx = 0;
		camera.rz = 0;
		camera.ry = 0;
		
		if(camera.z > 5) camera.z = 5;
		if(camera.z < 0.001) camera.z = 0.001f;
		if(camera.y > 3) camera.y = 3;
		if(camera.y < -3) camera.y = -3;
		if(camera.x > 3) camera.x = 3;
		if(camera.x < -3) camera.x = -3;
		
		Matrix4f viewMatrix = camera.getViewMatrix();
		Matrix4f projectionMatrix = camera.getProjectionMatrix(60, width, height);
		Matrix4f projectionView = projectionMatrix.mul(viewMatrix, new Matrix4f());
		
		TilingPattern pattern = null;
		
		if(pattern_index < 0) {
			pattern = customTiling;
		} else {
			if(!patterns.isEmpty()) {
				pattern = patterns.get(pattern_index);
			}
		}
		
		if(pattern != null) {
			int min_zoom = pattern.getMinimumZoom();
			int max_zoom = pattern.getMaximumZoom();
			
			{
				boolean key_U = Input.keys[GLFW_KEY_U];
				boolean key_I = Input.keys[GLFW_KEY_I];
				pressed = false;
				
				if(key_U || key_I) {
					if(!pressing) {
						pressing = true;
						
						if(key_I && zoom_level > min_zoom) {
							zoom_level --;
							pressed = true;
						}
						
						if(key_U && zoom_level < max_zoom) {
							zoom_level ++;
							pressed = true;
						}
					}
				} else pressing = false;
			}
			
			if(pattern_changed) {
				pattern_changed = false;
				pressed = true;
				zoom_level = min_zoom;
			}
			
			
			if(pressed) {
				pattern.generate(zoom_level);
			}
		}
		
		shader.bind();
		shader.setUniform("projectionView", projectionView);
		shader.setUniform("transformationMatrix", new Matrix4f());
		background.render();
		
		if(pattern != null && pattern.hasMesh()) {
			Matrix4f matrix = new Matrix4f();
			float deg = MathUtils.toRadians(360 / pattern.getSymmetry());
			
			for(int i = 0; i < pattern.getSymmetry(); i++) {
				matrix.rotateZ(deg);
				shader.setUniform("transformationMatrix", matrix);
				pattern.render();
			}
		}
		
		shader.unbind();
		
		GL11.glDisable(GL_DEPTH_TEST);
		gui.render();
		GL11.glEnable(GL_DEPTH_TEST);
	}
}
