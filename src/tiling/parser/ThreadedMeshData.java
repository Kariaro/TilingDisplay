package tiling.parser;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import tiling.util.MathUtils;

public class ThreadedMeshData {
	public final int faces;
	public FloatBuffer verts;
	public FloatBuffer uv;
	public FloatBuffer colors;
	public FloatBuffer matVerts;
	public FloatBuffer matColors;
	
	public ThreadedMeshData(int faces) {
		this.faces = faces;
		verts = MemoryUtil.memAllocFloat(faces * 9);
		uv = MemoryUtil.memAllocFloat(faces * 6);
		colors = MemoryUtil.memAllocFloat(faces * 12);
		matVerts = MemoryUtil.memAllocFloat(faces * 9 * 3);
		matColors = MemoryUtil.memAllocFloat(faces * 9 * 4);
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
		
		uv.put(tile.uv_array);
		colors.put(tile.colors_array);
		matColors.put(tile.matColors_array);
		
		float[] array = new float[9];
		for(int i = 0; i < tile.vertex.length; i++) {
			MathUtils.toFloatArray(translate(matrix, tile.vertex[i]), array);
			
			verts.put(array);
			matVerts.put(array);
			matVerts.put(array);
			matVerts.put(array);
		}
	}
	
	public void flip() {
		verts.flip();
		uv.flip();
		colors.flip();
		matVerts.flip();
		matColors.flip();
	}
	
	public void cleanup() {
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
