package render.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Face {
	public Material mat;
	
	public Vector3f[] vertex = new Vector3f[3];
	public Vector2f[] uv	 = new Vector2f[3];
	public Vector3f[] normal = new Vector3f[3];
	public Vector4f[] colors = new Vector4f[3];
	
	public Face() {
		
	}
}
