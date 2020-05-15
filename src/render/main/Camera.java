package render.main;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Matrix4f;

public class Camera {
	public float speed_mod = 1;
	
	public float xa;
	public float ya;
	public float za;
	
	public float x;
	public float y;
	public float z;
	
	public void update() {
		// Suggestion:
		// Shift / Control for zoom
		boolean forwards = Input.keys[GLFW_KEY_W];
		boolean right = Input.keys[GLFW_KEY_A];
		boolean left = Input.keys[GLFW_KEY_D];
		boolean backwards = Input.keys[GLFW_KEY_S];
		boolean up = Input.keys[GLFW_KEY_SPACE];
		boolean down = Input.keys[GLFW_KEY_LEFT_SHIFT];
		float speed = 0.002f * speed_mod;
		
		
		int xd = 0;
		int yd = 0;
		int zd = 0;
		
		if(forwards) zd --;
		if(backwards) zd ++;
		if(right) xd --;
		if(left) xd ++;
		if(up) yd ++;
		if(down) yd --;
		
		float xx = xd;
		float zz = zd;
		float yy = yd;
		
		x += xx * speed;
		y += yy * speed;
		z += zz * speed;
	}
	
	public Matrix4f getViewMatrix() {
		Matrix4f view = new Matrix4f();
		view.translate(-x, -y, -z);
		return view;
	}
	
	public Matrix4f getProjectionMatrix(float fov, float width, float height) {
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.setPerspective((float)Math.toRadians(fov), width / height, 0.0001f, 100);
		return projectionMatrix;
	}
}
