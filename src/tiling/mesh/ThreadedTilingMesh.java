package tiling.mesh;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import tiling.parser.ThreadedMeshData;

public class ThreadedTilingMesh implements Mesh {
	public final String name;
	public final Texture texture;
	
	public ThreadedTilingMesh(String name) {
		this(name, null);
	}
	
	public ThreadedTilingMesh(String name, Texture texture) {
		this.name = name;
		this.texture = texture;
	}
	
	public void buildObject(float[] v, float[] u, float[] c) {
		
	}
	
	public void buildObjectTiling(ThreadedMeshData data) {
		if(data.single) {
			buildObject(data);
		} else {
			data.flip();
			buildObject(
				data.faces,
				data.verts,
				data.uv,
				data.colors,
				data.matVerts,
				data.matColors
			);
		}
	}
	
	private int vaoId;
	public int vertexCount;
	
	private int vboVertex;
	private int vboUv;
	private int vboColor;
	
	
	private int vboMatVerts;
	private int vboMatColors;
	
	// TODO: Remove
	// The interleaved mesh was slower than the one with multiple buffers
	private int vboObject;
	
	private void buildObject(ThreadedMeshData data) {
		vertexCount = data.faces * 3;
		data.flip();
		
		try {
			vaoId = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vaoId);
			
			vboObject = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboObject);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data.object, GL15.GL_STATIC_DRAW);
			// [x, y, z], [u, v], [r, g, b, a], [x, y, z, x, y, z, x, y, z], [r, g, b, a, r, g, b, a, r, g, b, a]
			// 30 floats is one vertex
			// 90 floats per triangle
			// 
			// verts     :
			// uv        :
			// colors    :
			// matVerts  :
			// matColors :
			
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 30 * 4,      0);
			GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 30 * 4,  3 * 4);
			GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 30 * 4,  5 * 4);
			GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 30 * 4,  9 * 4);
			GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, false, 30 * 4, 12 * 4);
			GL20.glVertexAttribPointer(5, 3, GL11.GL_FLOAT, false, 30 * 4, 15 * 4);
			GL20.glVertexAttribPointer(6, 4, GL11.GL_FLOAT, false, 30 * 4, 18 * 4);
			GL20.glVertexAttribPointer(7, 4, GL11.GL_FLOAT, false, 30 * 4, 22 * 4);
			GL20.glVertexAttribPointer(8, 4, GL11.GL_FLOAT, false, 30 * 4, 26 * 4);
			
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
		} finally {
			
		}
	}
	
	@Override
	public void buildObject(int faces, FloatBuffer verts, FloatBuffer uv, FloatBuffer colors, FloatBuffer matVerts, FloatBuffer matColors) {
		vertexCount = faces * 3;
		try {
			vaoId = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vaoId);
			
			vboVertex = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertex);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verts, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0L);
			
			vboUv = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboUv);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uv, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0L);
			
			vboColor = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColor);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colors, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 0, 0L);
			
			vboMatVerts = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboMatVerts);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, matVerts, GL15.GL_STATIC_DRAW);
			
			
			GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 9 * 4, 4 * 0);
			GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, false, 9 * 4, 4 * 3);
			GL20.glVertexAttribPointer(5, 3, GL11.GL_FLOAT, false, 9 * 4, 4 * 6);
			
			
			vboMatColors = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboMatColors);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, matColors, GL15.GL_STATIC_DRAW);
			
			
			GL20.glVertexAttribPointer(6, 4, GL11.GL_FLOAT, false, 12 * 4, 4 * 0);
			GL20.glVertexAttribPointer(7, 4, GL11.GL_FLOAT, false, 12 * 4, 4 * 4);
			GL20.glVertexAttribPointer(8, 4, GL11.GL_FLOAT, false, 12 * 4, 4 * 8);
			
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
		} finally {
			
		}
	}

	@Override
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

	@Override
	public void cleanup() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboVertex);
		GL15.glDeleteBuffers(vboUv);
		GL15.glDeleteBuffers(vboColor);
		GL15.glDeleteBuffers(vboMatVerts);
		GL15.glDeleteBuffers(vboMatColors);
		//GL15.glDeleteBuffers(vboObject);
		
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
	}
}
