package tiling.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import tiling.mesh.Texture;
import tiling.mesh.ThreadedTilingMesh;
import tiling.util.TilingUtil;

public class TilingPattern {
	private static final Logger LOGGER = Logger.getLogger("Pattern");
	
	static {
		LOGGER.setLevel(Level.ALL);
	}
	
	protected ThreadedTilingMesh mesh;
	protected String default_tile;
	
	protected String name;
	protected String texturePath;
	
	protected int symmetry = 1;
	protected float scaling;
	protected float corner_weight = 0.2f;
	
	protected Vector3f startTransform = new Vector3f();
	protected float startRotation = 0;
	protected float startScale = 1;
	
	protected Map<String, TilingTile> tiles;
	protected List<Vector4f> colors;
	protected List<String> names;
	
	protected TilingProgram program;
	protected int maximum_zoom = 10;
	protected int minimum_zoom = 0;
	
	protected boolean debug;
	protected Vector4f[] debugColors = new Vector4f[] {
		new Vector4f(1, 0, 0, 1),
		new Vector4f(0, 1, 0, 1),
		new Vector4f(0, 0, 1, 1)
	};
	
	protected boolean show_corners;
	
	private Texture texture;
	
	public void setTexture(String path) {
		this.texturePath = path;
	}
	
	TilingPattern(TilingProgram program) {
		this.program = program;
		colors = new ArrayList<Vector4f>();
		names = new ArrayList<String>();
		tiles = new HashMap<String, TilingTile>();
	}
	
	protected void build() {
		LOGGER.finest("Building TilingPattern [" + name + "]");
		
		for(int i = 0; i < names.size(); i++) {
			String key = names.get(i);
			TilingTile tile = tiles.get(key);
			
			if(i < colors.size()) {
				tile.color = colors.get(i);
			} else {
				// TODO: Define default colors
				//tile.color = new Vector4f(0, 0, 0, 1);
				
				tile.color = new Vector4f(
						((i * 12323) & 0xff) / 255.0f,
					1 - ((i * 23213) & 0xff) / 255.0f,
					1, 1
				);
			}
			
			tile.build();
		}
	}
	
	public int getMaximumZoom() {
		return maximum_zoom;
	}
	
	public int getMinimumZoom() {
		return minimum_zoom;
	}
	
	public String getName() {
		return name;
	}
	
	public int getSymmetry() {
		return symmetry;
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public boolean hasMesh() {
		return mesh != null;
	}
	
	public boolean isDebugPattern() {
		return debug;
	}
	
	public float getScaling() {
		return scaling;
	}
	
	public float getStartScale() {
		return startScale;
	}
	
	public boolean hasTexture() {
		return texture != null;
	}
	
	public float getCurrentCornerScale() {
		return (float)Math.pow(scaling, mesh_zoom) * startScale * corner_weight;
	}

	public TilingTile getDefaultTile() {
		return tiles.getOrDefault(default_tile, tiles.get(names.get(0)));
	}
	
	public boolean isGenerating() {
		return generating;
	}
	
	public int getCurrentZoom() {
		return mesh_zoom;
	}
	
	private int mesh_zoom;
	public boolean generate(int max_depth) {
		if(max_depth < minimum_zoom) max_depth = minimum_zoom;
		if(max_depth > maximum_zoom) max_depth = maximum_zoom;
		
		TilingUtil.setDebugLevel(LOGGER);
		if(program.hasErrors()) {
			System.out.println("Program has errors!");
			return false;
		}
		
		generateMesh(max_depth);
		return true;
	}
	
	public void render() {
		if(mesh == null) return;
		mesh.render();
	}
	
	private boolean generating = false;
	private void generateMesh(int depth) {
		if(generating) return;
		generating = true;
		
		TilingUtil.executeWithTimeout(() -> {
			int zoom = depth;
			TilingTile tilingTile = getDefaultTile();
			
			{
				long faces = tilingTile.calculate_faces(zoom);
				if(faces < 0 || faces > 1000000) {
					LOGGER.warning("Mesh to big. " + faces + " faces");
					LOGGER.warning("The limit is 1000000 faces!");
					LOGGER.warning("");
					generating = false;
					return;
				}
				LOGGER.finer("Creating geometry: " + faces + " faces");
			}
			
			{
				LOGGER.finest("Generation Settings");
				LOGGER.finest("   zoom = " + zoom);
				LOGGER.finest("   default_tile = " + tilingTile);
				LOGGER.finest("");
				LOGGER.finest("Creating geometry");
			}
			
			final long geom_start = System.nanoTime();
			final ThreadedMeshData data = tilingTile.calculate(
				new Matrix4f().scale(startScale)
							  .translate(startTransform)
							  .rotateZ(startRotation), zoom
			);
			//System.out.printf("Total = [%.4f]\n", (data.total / 1000000.0));
			
			if(data == null) {
				generating = false;
				return;
			}
			
			//System.out.printf("Total time invokeLater: [%.4f] ms\n", (System.nanoTime() - geom_start) / 1000000.0);
			TilingUtil.invokeLater(() -> {
				if(mesh == null) {
					if(texturePath != null) {
						try {
							if(texturePath.startsWith("!")) { // Inside Jar
								texture = Texture.loadLocalTexture(texturePath.substring(1), GL11.GL_NEAREST);
							} else {
								texture = Texture.loadGlobalTexture(texturePath, GL11.GL_NEAREST);
							}
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
					
					mesh = new ThreadedTilingMesh(name + "_mesh", texture);
					mesh.buildObjectTiling(data);
				} else {
					mesh.cleanup();
					
					// This helps clean up memory
					//System.gc(); // 20 ms
					
					mesh.buildObjectTiling(data);
				}
				
				data.cleanup(); // 10 - 20 ms
				mesh_zoom = zoom;
				generating = false;
				
				LOGGER.finer(String.format("Took [%.4f] ms to generate mesh", (System.nanoTime() - geom_start) / 1000000.0));
			});
		}, 5, TimeUnit.SECONDS);
	}
	
	public void cleanup() {
		if(mesh != null) {
			mesh.cleanup();
		}
		
		if(texture != null) {
			texture.cleanup();
		}
		
		System.gc();
	}
}
