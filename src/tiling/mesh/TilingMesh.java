package tiling.mesh;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import tiling.parser.ThreadedMeshData;

public class TilingMesh implements Mesh {
	public final String name;
	public final Texture texture;
	
	public TilingMesh(String name) {
		this(name, null);
	}
	
	public TilingMesh(String name, Texture texture) {
		this.name = name;
		this.texture = texture;
	}
	
	private int vaoId;
	private int vertexCount;
	
	private int vboVertex;
	private int vboUv;
	private int vboColor;
	private int vboMatVerts;
	private int vboMatColors;
	
	public void buildObject(float[] verts, float[] uvs, float[] colors) {
		buildObject(verts, uvs, colors, null);
	}
	
	public void buildObject(float[] verts, float[] uvs, float[] colors, float[] matColors) {
		FloatBuffer verticesBuffer = null;
		FloatBuffer uvBuffer = null;
		FloatBuffer colorsBuffer = null;
		FloatBuffer matVertsBuffer = null;
		FloatBuffer matColorsBuffer = null;
		
		float[] matVerts = new float[verts.length * 3];
		for(int i = 0; i < verts.length / 9; i++) {
			for(int k = 0; k < 3; k++) {
				for(int j = 0; j < 9; j++) {
					matVerts[i * 27 + k * 9 + j] = verts[i * 9 + j];
				}
			}
		}
		
		if(matColors == null) {
			matColors = new float[colors.length * 3];
		}
		
		vertexCount = verts.length / 3;
		try {
			verticesBuffer = MemoryUtil.memAllocFloat(verts.length);
			verticesBuffer.put(verts).flip();
			
			uvBuffer = MemoryUtil.memAllocFloat(uvs.length);
			uvBuffer.put(uvs).flip();
			
			colorsBuffer = MemoryUtil.memAllocFloat(colors.length);
			colorsBuffer.put(colors).flip();
			
			matVertsBuffer = MemoryUtil.memAllocFloat(matVerts.length);
			matVertsBuffer.put(matVerts).flip();
			
			matColorsBuffer = MemoryUtil.memAllocFloat(matColors.length);
			matColorsBuffer.put(matColors).flip();
			
			
			
			vaoId = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vaoId);
			
			vboVertex = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertex);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0L);
			
			vboUv = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboUv);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0L);
			
			vboColor = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColor);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 0, 0L);
			
			vboMatVerts = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboMatVerts);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, matVertsBuffer, GL15.GL_STATIC_DRAW);
			
			
			GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 9 * 4, 4 * 0);
			GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, false, 9 * 4, 4 * 3);
			GL20.glVertexAttribPointer(5, 3, GL11.GL_FLOAT, false, 9 * 4, 4 * 6);
			
			
			vboMatColors = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboMatColors);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, matColorsBuffer, GL15.GL_STATIC_DRAW);
			
			
			GL20.glVertexAttribPointer(6, 4, GL11.GL_FLOAT, false, 12 * 4, 4 * 0);
			GL20.glVertexAttribPointer(7, 4, GL11.GL_FLOAT, false, 12 * 4, 4 * 4);
			GL20.glVertexAttribPointer(8, 4, GL11.GL_FLOAT, false, 12 * 4, 4 * 8);
			
			
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
		} finally {
			if(verticesBuffer != null) {
				MemoryUtil.memFree(verticesBuffer);
			}
			
			if(colorsBuffer != null) {
				MemoryUtil.memFree(colorsBuffer);
			}
			
			if(uvBuffer != null) {
				MemoryUtil.memFree(uvBuffer);
			}
			
			if(matVertsBuffer != null) {
				MemoryUtil.memFree(matVertsBuffer);
			}
			
			if(matColorsBuffer != null) {
				MemoryUtil.memFree(matColorsBuffer);
			}
		}
	}
	
	public void buildObject(int triangles, FloatBuffer verts, FloatBuffer uv, FloatBuffer colors, FloatBuffer matVerts, FloatBuffer matColors) {
	}
	
	public void render() {
		if(texture != null) {
			texture.bind();
		}
		
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		GL20.glEnableVertexAttribArray(5);
		
		GL20.glEnableVertexAttribArray(6);
		GL20.glEnableVertexAttribArray(7);
		GL20.glEnableVertexAttribArray(8);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		
		GL20.glDisableVertexAttribArray(8);
		GL20.glDisableVertexAttribArray(7);
		GL20.glDisableVertexAttribArray(6);
		
		GL20.glDisableVertexAttribArray(5);
		GL20.glDisableVertexAttribArray(4);
		GL20.glDisableVertexAttribArray(3);
		
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		
		if(texture != null) {
			texture.unbind();
		}
	}
	
	public void cleanup() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
		
		GL15.glDeleteBuffers(vboVertex);
		GL15.glDeleteBuffers(vboUv);
		GL15.glDeleteBuffers(vboColor);
		
		GL15.glDeleteBuffers(vboMatVerts);
		GL15.glDeleteBuffers(vboMatColors);
	}

	@Override
	public void buildObjectTiling(ThreadedMeshData data) {
		// TODO Auto-generated method stub
		
	}
}
