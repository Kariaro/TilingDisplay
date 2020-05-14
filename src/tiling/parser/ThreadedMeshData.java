package tiling.parser;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import tiling.util.MathUtils;

public class ThreadedMeshData {
	public final boolean single;
	public final int faces;
	public FloatBuffer object;
	public FloatBuffer verts;
	public FloatBuffer uv;
	public FloatBuffer colors;
	public FloatBuffer matVerts;
	public FloatBuffer matColors;
	
	public ThreadedMeshData(int faces) {
		this(faces, false);
	}
	
	public ThreadedMeshData(int faces, boolean single) {
		this.single = false;
		this.faces = faces;
		
		if(single) {
			object = MemoryUtil.memAllocFloat(faces * (9 + 9 + 6 + 12 + 9 * 3 + 9 * 4));
		} else {
			verts = MemoryUtil.memAllocFloat(faces * 9);
			uv = MemoryUtil.memAllocFloat(faces * 6);
			colors = MemoryUtil.memAllocFloat(faces * 12);
			matVerts = MemoryUtil.memAllocFloat(faces * 9 * 3);
			matColors = MemoryUtil.memAllocFloat(faces * 9 * 4);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		cleanup();
	}
	
	public long total = 0;
	public void write(TilingTile tile, Matrix4f _m) {
		Matrix4f matrix = new Matrix4f();
		_m.get(matrix);
		
		//if(!single) {
			uv.put(tile.uv_array);
			colors.put(tile.colors_array);
			matColors.put(tile.matColors_array);
			//tile.vertex[i].length * 3;
			float[] array = new float[9];
			//src.length * 3
			
			//long nanos = System.nanoTime();
			for(int i = 0; i < tile.vertex.length; i++) {
				//float[] array =
				MathUtils.toFloatArray(translate(matrix, tile.vertex[i]), array);
				
				verts.put(array);
				matVerts.put(array);
				matVerts.put(array);
				matVerts.put(array);
			}
			//total += System.nanoTime() - nanos;
		/*} else {
			// [x, y, z], [u, v], [r, g, b, a], [x, y, z, x, y, z, x, y, z], [r, g, b, a, r, g, b, a, r, g, b, a]
			// 30 floats is one vertex
			// 90 floats per triangle
			// 
			// verts     :
			// uv        :
			// colors    :
			// matVerts  :
			// matColors :
			
			
			for(int i = 0; i < tile.vertex.length; i++) {
				float[] array = MathUtils.toFloatArray(translate(matrix, tile.vertex[i]));
				
				// Vertex: 1
				object.put(array, 0, 3); // [xyz]
				object.put(tile.uv_array, i * 6, 2); // [uv]
				object.put(tile.colors_array, i * 12, 4); // [rgba]
				object.put(array); // [xyzxyzxyz]
				object.put(tile.matColors_array, i * 36, 12); // [rgbargbargba]
				
				
				// Vertex: 2
				object.put(array, 3, 3); // [xyz]
				object.put(tile.uv_array, i * 6 + 2, 2); // [uv]
				object.put(tile.colors_array, i * 12 + 4, 4); // [rgba]
				object.put(array); // [xyzxyzxyz]
				object.put(tile.matColors_array, i * 36 + 12, 12); // [rgbargbargba]
				
				
				// Vertex: 3
				object.put(array, 6, 3); // [xyz]
				object.put(tile.uv_array, i * 6 + 4, 2); // [uv]
				object.put(tile.colors_array, i * 12 + 8, 4); // [rgba]
				object.put(array); // [xyzxyzxyz]
				object.put(tile.matColors_array, i * 36 + 24, 12); // [rgbargbargba]
			}
		}*/
	}
	
	public void flip() {
		if(single) {
			object.flip();
		} else {
			verts.flip();
			uv.flip();
			colors.flip();
			matVerts.flip();
			matColors.flip();
		}
	}
	
	public void cleanup() {
		if(single) {
			if(object != null) {
				MemoryUtil.memFree(object);
				object = null;
			}
		} else {
			if(verts != null) {
				MemoryUtil.memFree(verts);
				verts = null;
			}
			if(uv != null) {
				MemoryUtil.memFree(uv);
				uv = null;
			}
			if(colors != null) {
				MemoryUtil.memFree(colors);
				colors = null;
			}
			if(matVerts != null) {
				MemoryUtil.memFree(matVerts);
				matVerts = null;
			}
			if(matColors != null) {
				MemoryUtil.memFree(matColors);
				matColors = null;
			}
		}
	}
	
	private Vector3f[] translate(Matrix4f _m, Vector3f[] vectors) {
		return new Vector3f[] {
			getPosition(_m, vectors[0], aaa),
			getPosition(_m, vectors[1], bbb),
			getPosition(_m, vectors[2], ccc)
		};
	}
	
	private static final Matrix4f POSITION_MAT = new Matrix4f();
	private static final Vector3f aaa = new Vector3f();
	private static final Vector3f bbb = new Vector3f();
	private static final Vector3f ccc = new Vector3f();
	private Vector3f getPosition(Matrix4f matrix, Vector3f offset, Vector3f dest) {
		return matrix.translate(offset, POSITION_MAT).getColumn(3, dest);
	}
}
