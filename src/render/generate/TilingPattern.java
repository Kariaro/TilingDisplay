package render.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import render.mesh.Face;
import render.mesh.TilingMesh;
import render.mesh.Texture;
import render.tiling.Tiling;

public class TilingPattern {
	private static final Vector2f[] zero_uv = new Vector2f[] { new Vector2f(0, 0), new Vector2f(0, 1),new Vector2f(1, 1) };
	
	private int zoom_max = Integer.MAX_VALUE;
	private int zoom_min = 0;
	
	private String default_tile_name;
	private TilingTile default_tile;
	private TilingMesh mesh;
	private String name;
	private Texture texture;
	
	private int symmetry = 1;
	float startScale = 1;
	float startRotation = 0;
	Vector3f startTransform = new Vector3f();
	boolean debugRotation;
	Vector3f[] debugColors = new Vector3f[] {
		new Vector3f(1, 0, 0),
		new Vector3f(0, 1, 0),
		new Vector3f(0, 0, 1)
	};
	float scaling;
	
	protected Map<String, TilingTile> tiles;
	protected List<Vector4f> colors;
	protected List<String> names;
	
	TilingPattern() {
		tiles = new HashMap<String, TilingTile>();
		names = new ArrayList<String>();
		
		colors = new ArrayList<Vector4f>();
	}
	
	protected void build() {
		if(Tiling.DEBUG) System.out.println("Building TilingPattern [" + name + "]");
		
		for(int i = 0; i < names.size(); i++) {
			String key = names.get(i);
			TilingTile tile = tiles.get(key);
			
			if(i < colors.size()) {
				tile.color = colors.get(i);
			}
			
			tile.build();
		}
		
		default_tile = tiles.getOrDefault(default_tile_name, tiles.get(names.get(0)));
	}
	
	public void render() {
		if(mesh == null) return;
		mesh.render();
	}
	
	public void setMinimumZoom(int min) {
		if(min < 0) {
			
		}
		this.zoom_min = min;
	}
	
	public void setMaximumZoom(int min) {
		this.zoom_max = min;
	}
	
	public void setDefaultTile(String tile_name) {
		this.default_tile_name = tile_name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setSymmetry(int value) {
		this.symmetry = value;
	}
	
	public void setTexture(Texture texture) {
		this.texture = texture;
	}
	
	
	public TilingTile getDefaultTile() {
		return default_tile;
	}
	
	public int getMinimumZoom() {
		return zoom_min;
	}
	
	public int getMaximumZoom() {
		return zoom_max;
	}
	
	public String getName() {
		return name;
	}
	
	public int getSymmetry() {
		if(symmetry < 1) return 1;
		return symmetry;
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public boolean hasMesh() {
		return mesh != null;
	}
	
	public void generate(int max_depth) {
		if(max_depth < zoom_min) max_depth = zoom_min;
		if(max_depth > zoom_max) max_depth = zoom_max;
		
		generateTilingMesh(max_depth);
	}
	
	private void generateTilingMesh(int zoom) {
		if(mesh != null) {
			mesh.cleanUp();
			
			// This helps clean up unreleased memory
			System.gc();
		} else {
			mesh = new TilingMesh(name + "_mesh", texture);
		}
		
		if(Tiling.DEBUG) {
			System.out.println("[Pattern] Generation Settings");
			System.out.printf("[Pattern]   zoom = %d\n", zoom);
			System.out.printf("[Pattern]   default_tile = %s\n", getDefaultTile());
			System.out.println("[Pattern]");
		}
		
		long start = System.nanoTime();
		List<TileMatrix4f> list = getDefaultTile().calculate(
			new Matrix4f().scale(startScale)
						  .translate(startTransform)
						  .rotateZ(startRotation), zoom
		);
		
		if(Tiling.DEBUG) {
			System.out.println("[Pattern] Creating geometry");
			if(list.size() == 1) System.out.printf("[Pattern] Creating geometry: generated 1 shape\n", list.size());
			else System.out.printf("[Pattern] Creating geometry: generated %d shapes\n", list.size());
			System.out.printf("[Pattern] Creating geometry: Time [%.5f ms]\n", (System.nanoTime() - start) / 1000000.0f);
			System.out.println("[Pattern]");
			System.out.println("[Pattern] Creating mesh");
		}
		
		long vbo_start = System.nanoTime();
		
		List<Face> faces = new ArrayList<Face>();
		
		Matrix4f matrix = new Matrix4f();
		for(TileMatrix4f tile_matrix : list) {
			TilingTile tile = tile_matrix.parent;
			Matrix4f _m = tile_matrix.matrix;
			
			for(int i = 0; i < tile.vertex.length; i++) {
				_m.get(matrix);
				
				Face face = new Face();
				if(tile.uv == null || tile.uv.length < 1) face.uv = zero_uv;
				else face.uv = tile.uv[i];
				face.vertex = translate(matrix, tile.vertex[i]);
				
				if(debugRotation) {
					Vector4f[] col = new Vector4f[3];
					for(int j = 0; j < 3; j++) {
						col[j] = new Vector4f(tile.colors[j]);
						col[j].x = (col[j].x / 2.0f + (debugColors[j].x / 2.0f));
						col[j].y = (col[j].y / 2.0f + (debugColors[j].y / 2.0f));
						col[j].z = (col[j].z / 2.0f + (debugColors[j].z / 2.0f));
					}
					face.colors = col;
				} else {
					face.colors = tile.colors;
				}
				
				faces.add(face);
			}
		}
		
		if(Tiling.DEBUG) {
			System.out.printf("[Pattern] Creating mesh: total_faces = %d\n", faces.size());
			System.out.printf("[Pattern] Creating mesh: Time [%.5f ms]\n", (System.nanoTime() - vbo_start) / 1000000.0f);
			System.out.println("[Pattern]");
			System.out.println("[Pattern] Creating VBO");
		}
		
		long build_start = System.nanoTime();
		mesh.buildObject(faces, debugRotation);
		
		if(Tiling.DEBUG) {
			System.out.printf("[Pattern] Creating VBO: Time [%.5f ms]\n", (System.nanoTime() - build_start) / 1000000.0f);
			System.out.println("[Pattern]");
			System.out.printf("[Pattern] Generating Mesh: Time [%.5f ms]\n", (System.nanoTime() - start) / 1000000.0f);
			System.out.println();
		}
	}
	
	public void cleanup() {
		if(mesh != null) {
			mesh.cleanUp();
		}
		if(texture != null) {
			texture.cleanUp();
		}
	}
	
	private Vector3f[] translate(Matrix4f _m, Vector3f[] vectors) {
		return new Vector3f[] {
			getPosition(_m, vectors[0]),
			getPosition(_m, vectors[1]),
			getPosition(_m, vectors[2])
		};
	}
	
	private Vector3f getPosition(Matrix4f matrix, Vector3f offset) {
		return matrix.translate(offset, new Matrix4f()).getColumn(3, new Vector3f());
	}
}
