package render.main;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import tiling.util.TilingUtil;

public class Input extends GLFWKeyCallback {
	public static boolean[] keys = new boolean[65536];
	
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		if(key < 0 || key >= keys.length) return;
		
		if(TilingUtil.checkBlockingInput(false)) {
			return;
		}
		
		keys[key] = (action != GLFW.GLFW_RELEASE) ? true:false;
	}
	
	public static boolean pollKey(int key) {
		if(key < 0 || key >= keys.length) return false;
		boolean pressed = keys[key];
		keys[key] = false;
		return pressed;
	}
}
