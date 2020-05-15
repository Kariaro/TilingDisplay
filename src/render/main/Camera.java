package render.main;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Matrix4f;

public class Camera {
	private float x;
	private float y;
	private float z;
	
	public Camera() {
		z = 3;
	}
	
	private long lastUpdate;
	public void update() {
		long now = System.currentTimeMillis();
		if(lastUpdate == 0) lastUpdate = now;
		float elapsed = 1;//(float)((now - lastUpdate) / 10.0);
		lastUpdate = now;
		
		// Suggestion:
		// Shift / Control for zoom
		boolean forwards = Input.keys[GLFW_KEY_W];
		boolean right = Input.keys[GLFW_KEY_A];
		boolean left = Input.keys[GLFW_KEY_D];
		boolean backwards = Input.keys[GLFW_KEY_S];
		boolean up = Input.keys[GLFW_KEY_SPACE];
		boolean down = Input.keys[GLFW_KEY_LEFT_SHIFT];
		
		float speed_mod = Math.abs(z) * 10;
		float speed = 0.002f * speed_mod * elapsed;
		
		
		int xx = 0;
		int yy = 0;
		int zz = 0;
		
		if(forwards) zz --;
		if(backwards) zz ++;
		if(right) xx --;
		if(left) xx ++;
		if(up) yy ++;
		if(down) yy --;
		
		x += xx * speed;
		y += yy * speed;
		z += zz * speed;
		
		if(y > 3) y = 3;
		if(y < -3) y = -3;
		if(x > 3) x = 3;
		if(x < -3) x = -3;

		if(z > 5) z = 5;
		if(z < 0.001) z = 0.001f;
	}
	
	public Matrix4f getProjectionViewMatrix(float fov, float width, float height) {
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.setPerspective((float)Math.toRadians(fov), width / height, 0.0001f, 100);
		return projectionMatrix.mul(new Matrix4f().translate(-x, -y, -z));
	}
}
