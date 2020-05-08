package render.mesh;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class TilingMesh {
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
	private int vboVertex;
	private int vboUv;
	private int vboColor;
	private int vertexCount;
	
	public void buildObject(List<Face> faces) {
		buildObject(faces, false);
	}
	
	public void buildObject(List<Face> faces, boolean debug) {
		//java.util.Random random = new java.util.Random(0);
		
		float[] v = new float[faces.size() * 9];
		float[] u = new float[faces.size() * 6];
		float[] c = new float[faces.size() * 12];
		
		Vector2f def_uv = new Vector2f(0, 0);
		Vector4f def_col = new Vector4f(1, 1, 1, 1);
		
		if(texture == null || debug) {
			for(int i = 0; i < faces.size(); i++) {
				Face face = faces.get(i);
				for(int j = 0; j < 3; j++) {
					v[i * 9 + j * 3    ] = face.vertex[j].x;
					v[i * 9 + j * 3 + 1] = face.vertex[j].y;
					v[i * 9 + j * 3 + 2] = face.vertex[j].z;
					
					Vector2f uv;
					if(j >= face.uv.length) {
						uv = def_uv;
					} else {
						uv = face.uv[j];
						if(uv  == null) uv = def_uv;
					}
					
					u[i * 6 + j * 2    ] = uv.x;
					u[i * 6 + j * 2 + 1] = uv.y;
					
					Vector4f color;
					if(j >= face.colors.length) {
						color = def_col;
					} else {
						color = face.colors[j];
						if(color == null) color = def_col;
					}
					
					c[i * 12 + j * 4    ] = color.x;
					c[i * 12 + j * 4 + 1] = color.y;
					c[i * 12 + j * 4 + 2] = color.z;
					c[i * 12 + j * 4 + 3] = color.w;
				}
			}
		} else {
			for(int i = 0; i < faces.size(); i++) {
				Face face = faces.get(i);
				for(int j = 0; j < 3; j++) {
					v[i * 9 + j * 3    ] = face.vertex[j].x;
					v[i * 9 + j * 3 + 1] = face.vertex[j].y;
					v[i * 9 + j * 3 + 2] = face.vertex[j].z;
					
					u[i * 6 + j * 2    ] = face.uv[j].x;
					u[i * 6 + j * 2 + 1] = face.uv[j].y;
					
					c[i * 12 + j * 4    ] = 0;
					c[i * 12 + j * 4 + 1] = 0;
					c[i * 12 + j * 4 + 2] = 0;
					c[i * 12 + j * 4 + 3] = 0;
				}
			}
		}
		
		
		buildObject(v, u, c);
	}
	
	public void buildObject(float[] verts, float[] uvs, float[] colors) {
		FloatBuffer verticesBuffer = null;
		FloatBuffer uvBuffer = null;
		FloatBuffer colorsBuffer = null;
		
		vertexCount = verts.length / 3;
		try {
			verticesBuffer = MemoryUtil.memAllocFloat(verts.length);
			verticesBuffer.put(verts).flip();
			
			uvBuffer = MemoryUtil.memAllocFloat(uvs.length);
			uvBuffer.put(uvs).flip();
			
			colorsBuffer = MemoryUtil.memAllocFloat(colors.length);
			colorsBuffer.put(colors).flip();
			
			
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
		}
	}
	
	public void render() {
		if(texture != null) {
			texture.bind();
		}
		
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		
		if(texture != null) {
			texture.unbind();
		}
	}
	
	public void cleanUp() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
		
		GL15.glDeleteBuffers(vboVertex);
		GL15.glDeleteBuffers(vboUv);
		GL15.glDeleteBuffers(vboColor);
		
		if(texture != null) {
			texture.cleanUp();
		}
	}
}
