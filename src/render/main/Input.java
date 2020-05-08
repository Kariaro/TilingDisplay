package render.main;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Input extends GLFWKeyCallback {
	public static boolean[] keys = new boolean[65536];
	
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		if(key < 0 || key >= keys.length) return;
		
		keys[key] = (action != GLFW.GLFW_RELEASE) ? true:false;
	}
}
