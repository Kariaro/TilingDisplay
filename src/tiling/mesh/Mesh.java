package tiling.mesh;

import java.nio.FloatBuffer;

import tiling.parser.ThreadedMeshData;

public interface Mesh {
	public void buildObject(float[] v, float[] u, float[] c);
	public void buildObject(int triangles, FloatBuffer verts, FloatBuffer uv, FloatBuffer colors, FloatBuffer matVerts, FloatBuffer matColors);
	public void buildObjectTiling(ThreadedMeshData data);
	public void render();
	public void cleanup();
}
