package render.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MathUtils {
	public static final float deg2Rad = (float)Math.PI / 180.0f;
	public static final float rad2Deg = (float)(180.0f / Math.PI);
	public static float cos(double a) {
		return (float)Math.cos(a);
	}
	
	public static float sin(double a) {
		return (float)Math.sin(a);
	}
	
	public static float cosDeg(double a) {
		return (float)Math.cos(a * deg2Rad);
	}
	
	public static float sinDeg(double a) {
		return (float)Math.sin(a * deg2Rad);
	}
	
	public static float pingPong(double a, double min, double max) {
		double x = a - min + (max - min) * 100;
		double mba = x % (max - min);
		boolean flip = ((int)(x / (max - min)) & 1) == 0;
		
		if(flip) {
			return (float)(mba + min);
		} else {
			return (float)(max - mba);
		}
	}
	
	public static float pingPong(double a, double offset, double min, double max) {
		double x = a + offset + (max - min) * 100;
		double mba = x % (max - min);
		boolean flip = ((int)(x / (max - min)) & 1) == 0;
		
		if(flip) {
			return (float)(mba + min);
		} else {
			return (float)(max - mba);
		}
	}
	
	
	public static Vector3f getPosition(Matrix4f matrix) {
		return getPosition(matrix, new Vector3f());
	}
	
	public static Vector3f getPosition(Matrix4f matrix, Vector3f dest) {
		return matrix.getColumn(3, dest);
	}
	
	public static Vector3f getRightDirection(Matrix4f matrix) {
		return getRightDirection(matrix, new Vector3f());
	}
	
	public static Vector3f getRightDirection(Matrix4f matrix, Vector3f dest) {
		return matrix.getColumn(0, dest);
	}
	
	public static Vector3f getUpDirection(Matrix4f matrix) {
		return getUpDirection(matrix, new Vector3f());
	}
	
	public static Vector3f getUpDirection(Matrix4f matrix, Vector3f dest) {
		return matrix.getColumn(1, dest);
	}
	
	public static Vector3f getLookDirection(Matrix4f matrix) {
		return getLookDirection(matrix, new Vector3f());
	}
	
	public static Vector3f getLookDirection(Matrix4f matrix, Vector3f dest) {
		return matrix.getColumn(2, dest);
	}
	
	public static float pingPongAngle(double a, double offset, double min, double max) {
		return pingPong(a, offset, min, max) * deg2Rad;
	}
	
	public static float clamp(double a, double min, double max) {
		if(a < min) return (float)min;
		if(a > max) return (float)max;
		return (float)a;
	}
	
	public static float abs(double a) {
		return (float)(a < 0 ? -a:a);
	}
	
	public static float toRadians(double deg) {
		return (float)(deg * deg2Rad);
	}
	/*
	public static Vector4f toVector4f(Vector3f v) { return new Vector4f(v, 0); }
	public static Vector4f toVector4f(Vector2f v) { return new Vector4f(v, 0, 0); }
	public static Vector3f toVector3f(Vector4f v) { return new Vector3f(v.x, v.y, v.z); }
	public static Vector3f toVector3f(Vector2f v) { return new Vector3f(v, 0); }
	public static Vector2f toVector2f(Vector4f v) { return new Vector2f(v.x, v.y); }
	public static Vector2f toVector2f(Vector3f v) { return new Vector2f(v.x, v.y); }
	*/
}