package render.main;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import tiling.util.MathUtils;

public class Camera {
	private final long window;
	public float x;
	public float y;
	public float z;
	
	public float xa;
	public float ya;
	public float za;
	
	public float rx;
	public float ry;
	public float rz;
	
	public float speed_mod = 1;
	
	public Camera(long window) {
		this.window = window;
	}
	
	private Vector2f mouse = new Vector2f(0, 0);
	private Vector2f delta = new Vector2f(0, 0);
	private void updateMouse() {
		// TODO: Use Mouse.java ?!
		double[] x = new double[1];
		double[] y = new double[1];
		glfwGetCursorPos(window, x, y);
		
		delta.x = mouse.x - (float)x[0];
		delta.y = mouse.y - (float)y[0];
		mouse.x = (float)x[0];
		mouse.y = (float)y[0];
	}
	
	public void update() {
		updateMouse();
		
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
		
		if(forwards) zd ++;
		if(backwards) zd --;
		if(right) xd --;
		if(left) xd ++;
		if(up) yd ++;
		if(down) yd --;
		
		float xx = xd * MathUtils.cosDeg(rx) + zd * MathUtils.sinDeg(rx);
		float zz = xd * MathUtils.sinDeg(rx) - zd * MathUtils.cosDeg(rx);
		float yy = yd;
		
		x += xx * speed;
		y += yy * speed;
		z += zz * speed;
		
		rx -= delta.x / 10.0f;
		ry -= delta.y / 10.0f;
		
		if(ry < -90) ry = -90;
		if(ry >  90) ry =  90;
		if(rx <   0) rx += 360;
		if(rx > 360) rx -= 360;
	}
	
	public Vector3f getPosition() {
		return new Vector3f(x, y, z);
	}
	
	public Vector3f getVelocity() {
		return new Vector3f(xa, ya, za);
	}
	
	public Vector3f getRotation() {
		return new Vector3f(rx, ry, rz);
	}
	
	public Matrix4f getTranslationMatrix() {
		Matrix4f view = new Matrix4f()
			.identity()
			.translate(-z, -x, -y)
			.rotateX((float)Math.toRadians(ry))
			.rotateY((float)Math.toRadians(rx))
			.rotateZ((float)Math.toRadians(rz));
		
		return view;
	}
	
	public Matrix4f getViewMatrix() {
		Matrix4f view = new Matrix4f();
		view.rotate((float)Math.toRadians(ry), new Vector3f(1, 0, 0));
		view.rotate((float)Math.toRadians(rx), new Vector3f(0, 1, 0));
		view.rotate((float)Math.toRadians(rz), new Vector3f(0, 0, 1));
		
		view.translate(-x, -y, -z);
		return view;
	}
	
	public Matrix4f getProjectionMatrix(float fov, float width, float height) {
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.setPerspective((float)Math.toRadians(fov), width / height, 0.0001f, 100);
		return projectionMatrix;
	}
	
	@Deprecated
	public void setTransform() {
		glRotatef(ry, 1, 0, 0);
		glRotatef(rx, 0, 1, 0);
		glRotatef(rz, 0, 0, 1);
		glTranslatef(-x, -y, -z);
	}
}
