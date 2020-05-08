package render.main;

import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

public class Shader {
	private final int programId;
	private int vertexShaderId;
	private int fragmentShaderId;
	
	private final Map<String, Integer> uniforms;
	
	public Shader() throws Exception {
		programId = glCreateProgram();
		if(programId == 0) {
			throw new Exception("Could not create Shader");
		}
		
		uniforms = new HashMap<String, Integer>();
	}
	
	public Shader(String vertexPath, String fragmentPath) throws Exception {
		programId = glCreateProgram();
		if(programId == 0) {
			throw new Exception("Could not create Shader");
		}
		
		createVertexShader(vertexPath);
		createFragmentShader(fragmentPath);
		link();
		
		uniforms = new HashMap<String, Integer>();
	}
	
	public void createVertexShader(String shaderPath) throws Exception {
		vertexShaderId = createShader(shaderPath, GL_VERTEX_SHADER);
	}
	
	public void createFragmentShader(String shaderPath) throws Exception {
		fragmentShaderId = createShader(shaderPath, GL_FRAGMENT_SHADER);
	}
	
	protected int createShader(String shaderPath, int shaderType) throws Exception {
		int shaderId = glCreateShader(shaderType);
		if(shaderId == 0) {
			throw new Exception("Error creating shader. Type: " + shaderType);
		}
		
		String shaderCode = FileUtils.readFile(shaderPath);
		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);
		
		if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}
		
		glAttachShader(programId, shaderId);
		return shaderId;
	}
	
	public int createShaderCode(String shaderCode, int shaderType) throws Exception {
		int shaderId = glCreateShader(shaderType);
		if(shaderId == 0) {
			throw new Exception("Error creating shader. Type: " + shaderType);
		}
		
		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);
		
		if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}
		
		glAttachShader(programId, shaderId);
		return shaderId;
	}
	
	public void link() throws Exception {
		glLinkProgram(programId);
		if(glGetProgrami(programId, GL_LINK_STATUS) == 0) {
			throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
		}
		
		if(vertexShaderId != 0) {
			glDetachShader(programId, vertexShaderId);
		}
		
		if(fragmentShaderId != 0) {
			glDetachShader(programId, fragmentShaderId);
		}
		
		glValidateProgram(programId);
		if(glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
			System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
		}
	}
	
	public void bindAttrib(int index, String name) {
		GL20.glBindAttribLocation(programId, index, name);
	}
	
	public void createUniform(String uniformName) throws Exception {
		int uniformLocation = glGetUniformLocation(programId, uniformName);
		if(uniformLocation < 0) {
			throw new Exception("Could not find uniform: " + uniformName);
		}
		
		uniforms.put(uniformName, uniformLocation);
	}
	
	public void setUniform(String uniformName, Matrix4f value) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer fb = stack.mallocFloat(16);
			value.get(fb);
			glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
		}
	}
	
	public void setUniform(String uniformName, Vector4f v) {
		glUniform4f(uniforms.get(uniformName), v.x, v.y, v.z, v.w);
	}
	
	public void setUniform(String uniformName, Vector3f v) {
		glUniform3f(uniforms.get(uniformName), v.x, v.y, v.z);
	}
	
	public void setUniform(String uniformName, Vector2f v) {
		glUniform2f(uniforms.get(uniformName), v.x, v.y);
	}
	
	public void setUniform(String uniformName, int value) {
		glUniform1i(uniforms.get(uniformName), value);
	}
	
	public void setUniform(String uniformName, float x, float y, float z, float w) {
		glUniform4f(uniforms.get(uniformName), x, y, z, w);
	}
	
	public void bind() {
		glUseProgram(programId);
	}
	
	public void unbind() {
		glUseProgram(0);
	}
	
	public void cleanup() {
		unbind();
		if(programId != 0) {
			glDeleteProgram(programId);
		}
	}
}
