package render.main;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import render.gui.Gui;
import tiling.mesh.TilingMesh;
import tiling.parser.TilingDefault;
import tiling.parser.TilingLoader;
import tiling.parser.TilingPattern;
import tiling.util.FileUtils;
import tiling.util.MathUtils;
import tiling.util.TilingUtil;

public class TilingRender {
	public final Tiling parent;
	private final long window;
	private int height;
	private int width;
	
	private TilingShader shader;
	private Camera camera;
	private Gui gui;
	
	private TilingMesh background;
	public List<TilingPattern> patterns;
	public TilingPattern customTiling;
	
	public TilingRender(Tiling parent, long window) {
		this.parent = parent;
		this.window = window;
		
		height = parent.getHeight();
		width = parent.getWidth();
		
		camera = new Camera(window);
		camera.z = 3;
		
		this.gui = new Gui(this);
		gui.height = height;
		gui.width = width;
		
		GL11.glDisable(GL_CULL_FACE);
		
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_PROJECTION_MATRIX);
		GL11.glOrtho(0, width, height, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW_MATRIX);
		
		try {
			createBackground();
			
			if(Tiling.DEBUG) {
				TilingUtil.execute(() -> {
					customTiling = TilingLoader.loadLocalPattern("/testing/DebugTiling.debug");
					pattern_index = -1;
					gui.setTiling(customTiling, new File("res/testing/DebugTiling.debug"));
				});
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// https://www.desmos.com/calculator/kf3xpyprsf
		patterns = TilingDefault.loadPatterns();
	}
	
	public void setViewport(int width, int height) {
		this.height = height;
		this.width = width;
		gui.width = width;
		gui.height = height;
		
		GL11.glViewport(0, 0, width, height);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_PROJECTION_MATRIX);
		GL11.glOrtho(0, width, height, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW_MATRIX);
	}
	
	private void createBackground() throws Exception {
		shader = new TilingShader();
		shader.createShaderCode(FileUtils.readStream(Tiling.class.getResourceAsStream("/shaders/tiling_fragment.fs")), GL20.GL_FRAGMENT_SHADER);
		shader.createShaderCode(FileUtils.readStream(Tiling.class.getResourceAsStream("/shaders/tiling_vertex.vs")), GL20.GL_VERTEX_SHADER);
		
		shader.bindAttrib(0, "in_Position");
		shader.bindAttrib(1, "in_Uv");
		shader.bindAttrib(2, "in_Color");
		
		shader.bindAttrib(3, "in_Verts_0");
		shader.bindAttrib(4, "in_Verts_1");
		shader.bindAttrib(5, "in_Verts_2");
		
		shader.bindAttrib(6, "in_Colors_0");
		shader.bindAttrib(7, "in_Colors_1");
		shader.bindAttrib(8, "in_Colors_2");
		
		
		shader.link();
		shader.createUniform("projectionView");
		shader.createUniform("transformationMatrix");
		shader.createUniform("hasTexture");
		shader.createUniform("isTiling");
		shader.createUniform("radius");
		
		background = new TilingMesh("background_mesh");
		Random random = new Random(0);
		
		List<Vector4f> colors = new ArrayList<>();
		List<Vector3f> vertices = new ArrayList<>();
		List<Vector2f> uvs = new ArrayList<>();
		
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
				uvs.add(new Vector2f(1, 1));
				vertices.add(new Vector3f(i + 1,  o, j + 1).mul(5));
				
				uvs.add(new Vector2f(1, 0));
				vertices.add(new Vector3f(i + 1,  o, j    ).mul(5));
				
				uvs.add(new Vector2f(0, 0));
				vertices.add(new Vector3f(i    ,  o, j    ).mul(5));
				
				uvs.add(new Vector2f(0, 1));
				vertices.add(new Vector3f(i    ,  o, j + 1).mul(5));
				
				uvs.add(new Vector2f(1, 1));
				vertices.add(new Vector3f(i + 1,  o, j + 1).mul(5));
				
				uvs.add(new Vector2f(0, 0));
				vertices.add(new Vector3f(i    ,  o, j    ).mul(5));
				
				uvs.add(new Vector2f(0, 0));
				vertices.add(new Vector3f(i    , -o, j    ).mul(5));
				
				uvs.add(new Vector2f(1, 0));
				vertices.add(new Vector3f(i + 1, -o, j    ).mul(5));
				
				uvs.add(new Vector2f(1, 1));
				vertices.add(new Vector3f(i + 1, -o, j + 1).mul(5));
				
				uvs.add(new Vector2f(0, 0));
				vertices.add(new Vector3f(i    , -o, j    ).mul(5));
				
				uvs.add(new Vector2f(1, 1));
				vertices.add(new Vector3f(i + 1, -o, j + 1).mul(5));
				
				uvs.add(new Vector2f(0, 1));
				vertices.add(new Vector3f(i    , -o, j + 1).mul(5));
			}
		}
		
		float g = -3f;
		for(int i = -5; i < 5; i++) {
			for(int k = -1; k < 2; k++) {
				uvs.add(new Vector2f(0, 0));
				vertices.add(new Vector3f(i    , k - 0.5f, g).mul(5));
				
				uvs.add(new Vector2f(1, 0));
				vertices.add(new Vector3f(i + 1, k - 0.5f, g).mul(5));
				
				uvs.add(new Vector2f(1, 1));
				vertices.add(new Vector3f(i + 1, k + 0.5f, g).mul(5));
				
				uvs.add(new Vector2f(0, 0));
				vertices.add(new Vector3f(i    , k - 0.5f, g).mul(5));
				
				uvs.add(new Vector2f(1, 1));
				vertices.add(new Vector3f(i + 1, k + 0.5f, g).mul(5));
				
				uvs.add(new Vector2f(0, 1));
				vertices.add(new Vector3f(i    , k + 0.5f, g).mul(5));
			}
		}
		
		//for(Vector4f color : colors) {
		//color.x *= 1.3f;
		//color.y *= 1.3f;
		//color.z *= 1.3f;
		//}
		
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
			
			Vector2f uv = uvs.get(i);
			u[i * 2    ] = uv.x;
			u[i * 2 + 1] = uv.y;
		}
		
		background.buildObject(v, u, c);
	}
	
	private boolean pattern_changed = true;
	private int pattern_index = 0;
	
	public int getFps() {
		return parent.getFps();
	}
	
	public int getPatternIndex() {
		return pattern_index;
	}
	
	public int getZoom() {
		return zoom_level;
	}
	
	public void resetZoom() {
		zoom_level = -1;
	}
	
	public void setZoom(int zoom) {
		this.zoom_level = zoom;
	}
	
	public void setPatternIndex(int index) {
		pattern_changed = true;
		pattern_index = index;
	}
	
	public void update() {
		TilingUtil.pollEvents();
		glfwSwapBuffers(window);
		glfwPollEvents();
		
		camera.speed_mod = Math.abs(camera.z) * 10;
		camera.update();
		
		camera.rx = 0;
		camera.rz = 0;
		camera.ry = 0;
		
		if(camera.z > 5) camera.z = 5;
		if(camera.z < 0.001) camera.z = 0.001f;
		if(camera.y > 3) camera.y = 3;
		if(camera.y < -3) camera.y = -3;
		if(camera.x > 3) camera.x = 3;
		if(camera.x < -3) camera.x = -3;
	}
	
	//private Texture texture;
	private int zoom_level = 0;
	public void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
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
			boolean pressed = false;
			int min_zoom = pattern.getMinimumZoom();
			int max_zoom = pattern.getMaximumZoom();
			
			{
				boolean key_U = Input.pollKey(GLFW_KEY_U);
				boolean key_I = Input.pollKey(GLFW_KEY_I);
				
				if(key_I && zoom_level > min_zoom) {
					zoom_level --;
					pressed = true;
				}
				
				if(key_U && zoom_level < max_zoom) {
					zoom_level ++;
					pressed = true;
				}
			}
			
			if(pattern_changed) {
				pattern_changed = false;
				pressed = true;
				
				if(zoom_level > max_zoom) zoom_level = max_zoom;
				if(zoom_level < min_zoom) zoom_level = min_zoom;
			}
			
			
			if(pressed) {
				pattern.generate(zoom_level);
			}
		}
		
		//if(texture == null) {
			//texture = new Texture(Tiling.class.getResourceAsStream("/texture/Background.png"));
		//}
		GL11.glEnable(GL_DEPTH_TEST);
		shader.bind();
		shader.setUniform("projectionView", projectionView);
		shader.setUniform("transformationMatrix", new Matrix4f());
		shader.setUniform("hasTexture", false);
		
		shader.setUniform("isTiling", false);
		//texture.bind();
		background.render();
		//texture.unbind();
		
		if(pattern != null && pattern.hasMesh()) {
			Matrix4f matrix = new Matrix4f();
			float deg = MathUtils.toRadians(360 / (float)pattern.getSymmetry());
			
			shader.setUniform("hasTexture", pattern.hasTexture());
			shader.setUniform("isTiling", true);
			shader.setUniform("radius", pattern.getCurrentCornerScale());
			
			for(int i = 0; i < pattern.getSymmetry(); i++) {
				matrix.rotateZ(deg);
				shader.setUniform("transformationMatrix", matrix);
				pattern.render();
			}
		}
		shader.unbind();
		GL11.glDisable(GL_DEPTH_TEST);
		
		// TODO: Optimize GUI
		gui.render();
	}
}